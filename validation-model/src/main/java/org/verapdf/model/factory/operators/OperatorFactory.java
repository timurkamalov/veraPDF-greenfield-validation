package org.verapdf.model.factory.operators;

import org.apache.log4j.Logger;
import org.verapdf.cos.COSBase;
import org.verapdf.model.impl.pd.util.PDResourcesHandler;
import org.verapdf.operator.Operator;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for converting raw operators to the veraPDF-library operators
 *
 * @author Timur Kamalov
 */
public final class OperatorFactory {

    private static final Logger LOGGER = Logger.getLogger(OperatorFactory.class);

    private static final String MSG_UNEXPECTED_OBJECT_TYPE = "Unexpected type of object in tokens: ";

    private boolean isLastParsedContainsTransparency = false;

    private static final Map<String, TransparencyBehaviour> PAINT_OPERATORS_WITHOUT_TEXT;
    static {
        Map<String, TransparencyBehaviour> aMap = new HashMap<>();
        TransparencyBehaviour fill = TransparencyBehaviour.createFillInstance();
        TransparencyBehaviour fillXObject = TransparencyBehaviour.createFillXObjectInstance();
        TransparencyBehaviour fillCS = TransparencyBehaviour.createFillColorSpaceInstance();
        TransparencyBehaviour strokeCS = TransparencyBehaviour.createStrokeColorSpaceInstance();
        TransparencyBehaviour fillStrokeCS = TransparencyBehaviour.createFillStrokeColorSpaceInstance();
        aMap.put(Operators.S_STROKE, strokeCS);
        aMap.put(Operators.S_CLOSE_STROKE, strokeCS);
        aMap.put(Operators.F_FILL, fillCS);
        aMap.put(Operators.F_FILL_OBSOLETE, fillCS);
        aMap.put(Operators.F_STAR_FILL, fillCS);
        aMap.put(Operators.B_FILL_STROKE, fillStrokeCS);
        aMap.put(Operators.B_STAR_EOFILL_STROKE, fillStrokeCS);
        aMap.put(Operators.B_CLOSEPATH_FILL_STROKE, fillStrokeCS);
        aMap.put(Operators.B_STAR_CLOSEPATH_EOFILL_STROKE, fillStrokeCS);
        aMap.put(Operators.SH, fill);
        aMap.put(Operators.DO, fillXObject);
        aMap.put(Operators.EI, fill);
        PAINT_OPERATORS_WITHOUT_TEXT = Collections.unmodifiableMap(aMap);
    }

    private static final Set<String> PAINT_OPERATORS_TEXT = new HashSet<>(Arrays.asList(new String[]{
            Operators.TJ_SHOW,
            Operators.QUOTE,
            Operators.DOUBLE_QUOTE,
            Operators.TJ_SHOW_POS
    }));

    private static final Map<RenderingMode, TransparencyBehaviour> RENDERING_MODE;
    static {
        Map<RenderingMode, TransparencyBehaviour> aMap = new HashMap<>();
        TransparencyBehaviour strokeCSFont = TransparencyBehaviour.createStrokeColorSpaceFontInstance();
        TransparencyBehaviour fillCSFont = TransparencyBehaviour.createFillColorSpaceFontInstance();
        TransparencyBehaviour fillStrokeCSFont = TransparencyBehaviour.createFillStrokeColorSpaceFontInstance();
        aMap.put(RenderingMode.FILL, fillCSFont);
        aMap.put(RenderingMode.STROKE, strokeCSFont);
        aMap.put(RenderingMode.FILL_STROKE, fillStrokeCSFont);
        aMap.put(RenderingMode.FILL_CLIP, fillCSFont);
        aMap.put(RenderingMode.STROKE_CLIP, strokeCSFont);
        aMap.put(RenderingMode.FILL_STROKE_CLIP, fillStrokeCSFont);
        RENDERING_MODE = Collections.unmodifiableMap(aMap);
    }

    /**
     * @return true if during the last call of parsing method there was any transparency
     */
    public boolean isLastParsedContainsTransparency() {
        return isLastParsedContainsTransparency;
    }

    public List<org.verapdf.model.operator.Operator> operatorsFromTokens(List<Object> rawTokens, PDResourcesHandler resourcesHandler) {
        List<org.verapdf.model.operator.Operator> result = new ArrayList<>();
        List<COSBase> arguments = new ArrayList<>();
        this.isLastParsedContainsTransparency = false;
        OperatorParser parser = new OperatorParser();

        for (Object rawToken : rawTokens) {
            if (rawToken instanceof COSBase) {
                arguments.add((COSBase) rawToken);
            } else if (rawToken instanceof Operator) {
                parser.parseOperator(result, ((Operator) rawToken), arguments);

                String parsedOperatorType = ((Operator) rawToken).getOperator();
                TransparencyGraphicsState graphicState = parser.getTransparencyGraphicState();
                if (PAINT_OPERATORS_WITHOUT_TEXT.containsKey(parsedOperatorType)) {
                    isLastParsedContainsTransparency |= PAINT_OPERATORS_WITHOUT_TEXT.get(parsedOperatorType).containsTransparency(graphicState);
                } else {
                    RenderingMode renderingMode = parser.getGSRenderingMode();
                    if (PAINT_OPERATORS_TEXT.contains(parsedOperatorType) && RENDERING_MODE.containsKey(renderingMode)) {
                        isLastParsedContainsTransparency |= RENDERING_MODE.get(renderingMode).containsTransparency(graphicState);
                    }
                }

                arguments = new ArrayList<>();
            } else {
                LOGGER.debug(MSG_UNEXPECTED_OBJECT_TYPE
                        + rawToken.getClass().getName());
            }
        }
        return result;
    }
}
