/**
 * This file is part of veraPDF Validation, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Validation is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Validation as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Validation as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.gf.model.impl.pd.util;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSName;
import org.verapdf.pd.PDExtGState;
import org.verapdf.pd.PDResource;
import org.verapdf.pd.PDResources;
import org.verapdf.pd.colors.PDColorSpace;
import org.verapdf.pd.colors.PDDeviceCMYK;
import org.verapdf.pd.colors.PDDeviceGray;
import org.verapdf.pd.colors.PDDeviceRGB;
import org.verapdf.pd.font.PDFont;
import org.verapdf.pd.images.PDXObject;
import org.verapdf.pd.patterns.PDPattern;
import org.verapdf.pd.patterns.PDShading;

/**
 * @author Timur Kamalov
 */
public class PDResourcesHandler {

	public static final PDResourcesHandler EMPTY = PDResourcesHandler.getInstance(new PDResources(COSDictionary.construct()), false);

	private final PDResources pageResources;
	private final boolean inheritedResources;

	private final PDResources objectResources;

	private boolean containsUndefinedResource = false;

	private PDResourcesHandler(PDResources pageResources, boolean inheritedResources) {
		this.pageResources = pageResources;
		this.inheritedResources = inheritedResources;

		this.objectResources = null;
	}

	private PDResourcesHandler(PDResources pageResources, PDResources objectResources) {
		this.pageResources = pageResources;
		this.inheritedResources = false;

		this.objectResources = objectResources;
	}

	public static PDResourcesHandler getInstance(PDResources resources, boolean inheritedResources) {
		return new PDResourcesHandler(resources, inheritedResources);
	}

	public static PDResourcesHandler getInstance(PDResources pageResources, PDResources objectResources) {
		return new PDResourcesHandler(pageResources, objectResources);
	}

	//Used for XObjects
	public PDResourcesHandler getExtendedResources(PDResources objectResources) {
		return getInstance(this.pageResources, objectResources);
	}

	public PDFont getFont(COSName name) {
		if (name != null) {
			return getFont(name.getName());
		}
		return null;
	}

	public PDFont getFont(ASAtom name) {
		PDFont font = null;
		if (this.objectResources != null) {
			font = this.objectResources.getFont(name);
			if (font == null && this.pageResources != null) {
				font = this.pageResources.getFont(name);
				setInherited(font, true);
			}
		} else if (this.pageResources != null) {
			font = this.pageResources.getFont(name);
			setInherited(font, inheritedResources);
		}
		if (font == null) {
			containsUndefinedResource = true;
		}
		return font;
	}

	public PDColorSpace getColorSpace(COSName name) {
		if (name != null) {
			return getColorSpace(name.getName());
		}
		return null;
	}

	public PDColorSpace getColorSpace(ASAtom name) {
		PDColorSpace colorSpace = null;
		if (this.objectResources != null) {
			if (isDefaultColorSpaceUsed(name)) {
				colorSpace = this.objectResources.getDefaultColorSpace(name);
			} else {
				colorSpace = this.objectResources.getColorSpace(name);
				if (colorSpace == null && this.pageResources != null) {
					colorSpace = this.pageResources.getColorSpace(name);
					colorSpace = setColorSpaceInherited(colorSpace, true);
				}
			}
		} else if (this.pageResources != null) {
			if (isDefaultColorSpaceUsed(name)) {
				colorSpace = this.pageResources.getDefaultColorSpace(name);
			} else {
				colorSpace = this.pageResources.getColorSpace(name);
				colorSpace = setColorSpaceInherited(colorSpace, inheritedResources);
			}
		}
		if (colorSpace == null) {
			containsUndefinedResource = true;
		}
		return colorSpace;
	}

	public PDPattern getPattern(COSName name) {
		if (name != null) {
			return getPattern(name.getName());
		}
		return null;
	}

	public PDPattern getPattern(ASAtom name) {
		PDPattern pattern = null;
		if (this.objectResources != null) {
			pattern = this.objectResources.getPattern(name);
			if (pattern == null && this.pageResources != null) {
				pattern = this.pageResources.getPattern(name);
				setInherited(pattern, true);
			}
		} else if (this.pageResources != null){
			pattern = this.pageResources.getPattern(name);
			setInherited(pattern, inheritedResources);
		}
		if (pattern == null) {
			containsUndefinedResource = true;
		}
		return pattern;
	}

	public PDShading getShading(COSName name) {
		if (name != null) {
			return getShading(name.getName());
		}
		return null;
	}

	public PDShading getShading(ASAtom name) {
		PDShading shading = null;
		if (this.objectResources != null) {
			shading = this.objectResources.getShading(name);
			if (shading == null && this.pageResources != null) {
				shading = this.pageResources.getShading(name);
				setInherited(shading, true);
			}
		} else if (this.pageResources != null) {
			shading = this.pageResources.getShading(name);
			setInherited(shading, inheritedResources);
		}
		if (shading == null) {
			containsUndefinedResource = true;
		}
		return shading;
	}

	public PDXObject getXObject(COSName name) {
		if (name != null) {
			return getXObject(name.getName());
		}
		return null;
	}

	public PDXObject getXObject(ASAtom name) {
		PDXObject xObject = null;
		if (this.objectResources != null) {
			xObject = this.objectResources.getXObject(name);
			if (xObject == null && this.pageResources != null) {
				xObject = this.pageResources.getXObject(name);
				setInherited(xObject, true);
			}
		} else if (this.pageResources != null) {
			xObject = this.pageResources.getXObject(name);
			setInherited(xObject, inheritedResources);
		}
		if (xObject == null) {
			containsUndefinedResource = true;
		}
		return xObject;
	}

	public PDExtGState getExtGState(COSName name) {
		if (name != null) {
			return getExtGState(name.getName());
		}
		return null;
	}

	public PDExtGState getExtGState(ASAtom name) {
		PDExtGState state = null;
		if (this.objectResources != null) {
			state = this.objectResources.getExtGState(name);
			if (state == null && this.pageResources != null) {
				state = this.pageResources.getExtGState(name);
				setInherited(state, true);
			}
		} else if (this.pageResources != null) {
			state = this.pageResources.getExtGState(name);
			setInherited(state, inheritedResources);
		}
		if (state == null) {
			containsUndefinedResource = true;
		}
		return state;
	}

	public PDResource getProperties(COSName name) {
		if (name != null) {
			return getProperties(name.getName());
		}
		return null;
	}

	public PDResource getProperties(ASAtom name) {
		PDResource res = null;
		if (this.objectResources != null) {
			res = this.objectResources.getProperties(name);
			if (res == null && this.pageResources != null) {
				res = this.pageResources.getProperties(name);
				setInherited(res, true);
			}
		} else if (this.pageResources != null) {
			res = this.pageResources.getProperties(name);
			setInherited(res, inheritedResources);
		}
		if (res == null) {
			containsUndefinedResource = true;
		}
		return res;
	}

	public PDColorSpace setColorSpaceInherited(PDColorSpace colorSpace, boolean isInherited) {
		if (isInherited) {
			if (colorSpace == PDDeviceCMYK.INSTANCE) {
				return PDDeviceCMYK.INHERITED_INSTANCE;
			} else if (colorSpace == PDDeviceRGB.INSTANCE) {
				return PDDeviceRGB.INHERITED_INSTANCE;
			} else if (colorSpace == PDDeviceGray.INSTANCE) {
				return PDDeviceGray.INHERITED_INSTANCE;
			}
		}
		setInherited(colorSpace, isInherited);
		return colorSpace;

	}

	public void setInherited(PDResource resource, boolean value) {
		if (resource != null) {
			resource.setInherited(value);
		}
	}

	public PDResources getObjectResources() {
		return this.objectResources;
	}

	public PDResources getPageResources() {
		return this.pageResources;
	}

	private boolean isDefaultColorSpaceUsed(ASAtom name) {
		if (PDResourcesHandler.isDeviceDependent(name)) {
			if (objectResources != null) {
				ASAtom value = org.verapdf.factory.colors.ColorSpaceFactory.getDefaultValue(objectResources, name);
				if (value != null) {
					return true;
				}
			} else {
				ASAtom value = org.verapdf.factory.colors.ColorSpaceFactory.getDefaultValue(pageResources, name);
				if (value != null) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isDeviceDependent(ASAtom name) {
		return ASAtom.DEVICERGB.equals(name) ||
				ASAtom.DEVICEGRAY.equals(name) || ASAtom.DEVICECMYK.equals(name);
	}

	public boolean getContainsUndefinedResource() {
		return containsUndefinedResource;
	}
}
