package edu.montana.csci.csci468.parser.statements;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.parser.expressions.Expression;

public class VariableStatement extends Statement {
    private Expression expression;
    private String variableName;
    private CatscriptType explicitType;
    private CatscriptType type;

    public Expression getExpression() {
        return expression;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public void setExpression(Expression parseExpression) {
        this.expression = addChild(parseExpression);
    }

    public void setExplicitType(CatscriptType type) {
        this.explicitType = type;
    }

    public CatscriptType getExplicitType() {
        return explicitType;
    }

    public boolean isGlobal() {
        return getParent() instanceof CatScriptProgram;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        expression.validate(symbolTable);
        if (symbolTable.hasSymbol(variableName)) {
            addError(ErrorType.DUPLICATE_NAME);
        }
        // ensure explicit type is correct type
        else if (explicitType != null) {
            // ensure

            boolean result;
//            if (CatscriptType.getListType(explicitType) != null) {
//                CatscriptType test = CatscriptType.getListType(explicitType);
//                result = CatscriptType.getListType(explicitType) == expression.getType();
//            } else {
//                result = (explicitType == expression.getType());
                result = (explicitType.isAssignableFrom(expression.getType()));
//            }
            // fix: putting object here feels hackey
            if (!result && explicitType != CatscriptType.OBJECT) {
                addError(ErrorType.INCOMPATIBLE_TYPES);
            }
            else {
                type = explicitType;
                symbolTable.registerSymbol(variableName, explicitType);
            }
        }
        // infer type from expression
        else {
            type = expression.getType();
//            explicitType = expression.getType(); // do I set an explit type here now?
            symbolTable.registerSymbol(variableName, type);
        }
    }

    public CatscriptType getType() {
        return type;
    }

    //==============================================================
    // Implementation
    //==============================================================
    @Override
    public void execute(CatscriptRuntime runtime) {
        super.execute(runtime);
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        super.compile(code);
    }
}
