package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenType;

public class LogicalExpression extends Expression {

    private final Expression lefthandside;
    private final Expression righthandside;
    private final Token operator;

    public LogicalExpression(Token operator, Expression righthand, Expression lefthand) {
        this.operator = operator;
        lefthandside = lefthand;
        righthandside = righthand;
    }

    public boolean isAnd() {
        return operator.getType() == TokenType.LAND;
    }

    // restrict right hand and lefthand side to boolean
    @Override
    public void validate(SymbolTable symbolTable) {
        if (!lefthandside.getType().equals(CatscriptType.BOOLEAN)) {
            lefthandside.addError(ErrorType.INCOMPATIBLE_TYPES);
        }
        if (!righthandside.getType().equals(CatscriptType.BOOLEAN)) {
            righthandside.addError(ErrorType.INCOMPATIBLE_TYPES);
        }
    }

    @Override
    public CatscriptType getType() {
        return CatscriptType.BOOLEAN;
    }

    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(CatscriptRuntime runtime) {
        Boolean rhs = (Boolean) righthandside.evaluate(runtime);
        Boolean lhs = (Boolean) lefthandside.evaluate(runtime);
        if (isAnd()) {
            return rhs && lhs;
        }
        else {
            return rhs || lhs;
        }
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
