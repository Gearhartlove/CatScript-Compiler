package edu.montana.csci.csci468.parser;

import edu.montana.csci.csci468.parser.expressions.*;
import edu.montana.csci.csci468.parser.statements.*;
import edu.montana.csci.csci468.tokenizer.CatScriptTokenizer;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenList;
import edu.montana.csci.csci468.tokenizer.TokenType;

import java.util.LinkedList;
import java.util.List;

import static edu.montana.csci.csci468.tokenizer.TokenType.*;

public class CatScriptParser {

    private TokenList tokens;
    private FunctionDefinitionStatement currentFunctionDefinition;

    public CatScriptProgram parse(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();

        // first parse an expression
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = null;
        try {
            expression = parseExpression();
        } catch(RuntimeException re) {
            // ignore :)
        }
        if (expression == null || tokens.hasMoreTokens()) {
            tokens.reset();
            while (tokens.hasMoreTokens()) {
                program.addStatement(parseProgramStatement());
            }
        } else {
            program.setExpression(expression);
        }

        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    public CatScriptProgram parseAsExpression(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = parseExpression();
        program.setExpression(expression);
        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    //============================================================
    //  Statements
    //============================================================

    private Statement parseProgramStatement() {
        Statement printStmt = parsePrintStatement();
        if (printStmt != null) {
            return printStmt;
        }
        return new SyntaxErrorStatement(tokens.consumeToken());
    }

    private Statement parsePrintStatement() {
        if (tokens.match(PRINT)) {

            PrintStatement printStatement = new PrintStatement();
            printStatement.setStart(tokens.consumeToken());

            require(LEFT_PAREN, printStatement);
            printStatement.setExpression(parseExpression());
            printStatement.setEnd(require(RIGHT_PAREN, printStatement));

            return printStatement;
        } else {
            return null;
        }
    }

    //============================================================
    //  Expressions
    //============================================================

    private Expression parseExpression() {
        // ComparisonExpression ?

        return parseAdditiveExpression();
    }

    private Expression parseAdditiveExpression() {
        Expression expression = parseUnaryExpression();
        Expression rightHandSide;
        while (tokens.match(STAR, SLASH)) {
            Token operator = tokens.consumeToken();
            if (tokens.match(LEFT_PAREN)) {
                tokens.consumeToken();
                rightHandSide = new ParenthesizedExpression(parseAdditiveExpression());
                tokens.consumeToken(); // consume right paren
            } else {
                rightHandSide = parseUnaryExpression();
            }
            FactorExpression factorExpression = new FactorExpression(operator, expression, rightHandSide);
            factorExpression.setStart(expression.getStart());
            factorExpression.setEnd(rightHandSide.getEnd());
            expression = factorExpression;
        }
        while (tokens.match(PLUS, MINUS)) {
            Token operator = tokens.consumeToken();
            if (tokens.match(LEFT_PAREN)) {
                tokens.consumeToken();
                rightHandSide = new ParenthesizedExpression(parseAdditiveExpression());
                tokens.consumeToken(); // consume right paren
            } else {
                rightHandSide = parseUnaryExpression();
            }
            AdditiveExpression additiveExpression = new AdditiveExpression(operator, expression, rightHandSide);
            additiveExpression.setStart(expression.getStart());
            additiveExpression.setEnd(rightHandSide.getEnd());
            expression = additiveExpression;
        }

        return expression;
    }

    private Expression parseUnaryExpression() {
        if (tokens.match(MINUS, NOT)) {
            Token token = tokens.consumeToken();
            Expression rhs = parseUnaryExpression();
            UnaryExpression unaryExpression = new UnaryExpression(token, rhs);
            unaryExpression.setStart(token);
            unaryExpression.setEnd(rhs.getEnd());
            return unaryExpression;
        } else {
            return parsePrimaryExpression();
        }
    }

    // TODO: implement functional_call, expression
    private Expression parsePrimaryExpression() {
        if (tokens.match(INTEGER)) {
            Token integerToken = tokens.consumeToken();
            IntegerLiteralExpression integerExpression = new IntegerLiteralExpression(integerToken.getStringValue());
            integerExpression.setToken(integerToken);
            return integerExpression;
        } else if (tokens.match(TRUE)){
            Token booleanToken = tokens.consumeToken();
            BooleanLiteralExpression booleanExpression = new BooleanLiteralExpression(Boolean.parseBoolean(booleanToken.getStringValue()));
            booleanExpression.setToken(booleanToken);
            return booleanExpression;
        } else if (tokens.match(FALSE)) {
            Token booleanToken = tokens.consumeToken();
            BooleanLiteralExpression booleanExpression = new BooleanLiteralExpression(Boolean.parseBoolean(booleanToken.getStringValue()));
            booleanExpression.setToken(booleanToken);
            return booleanExpression;
        } else if (tokens.match(IDENTIFIER)) {
            Token identifierToken = tokens.consumeToken();
            if (tokens.match(LEFT_PAREN)) {
                tokens.consumeToken();
                // function logic
                LinkedList<Expression> arguments = new LinkedList<>();
                while (!tokens.match(RIGHT_PAREN) && !tokens.match(EOF)) {

                    Expression e = parseExpression(); // return the argument
                    arguments.add(e);
                    if (tokens.match(EOF)) {
                        FunctionCallExpression functionCallExpression =
                                new FunctionCallExpression(identifierToken.getStringValue(), arguments);
                        functionCallExpression.addError(ErrorType.UNTERMINATED_ARG_LIST);
                        return functionCallExpression;
                    }
                    tokens.consumeToken(); // consume the '(', or the ','
                }
                FunctionCallExpression functionCallExpression =
                        new FunctionCallExpression(identifierToken.getStringValue(), arguments);
                return functionCallExpression;
            } else {
                IdentifierExpression identExpression = new IdentifierExpression(identifierToken.getStringValue());
                identExpression.setToken(identifierToken);
                return identExpression;
            }
        } else if (tokens.match(STRING)) {
            Token stringToken = tokens.consumeToken();
            StringLiteralExpression stringExpression = new StringLiteralExpression(stringToken.getStringValue());
            stringExpression.setToken(stringToken);
            return stringExpression;
        } else if (tokens.match(NULL)) {
            Token nullToken = tokens.consumeToken();
            NullLiteralExpression nullLiteralExpression = new NullLiteralExpression();
            nullLiteralExpression.setToken(nullToken);
            return nullLiteralExpression;
        } else if (tokens.match(LEFT_BRACKET)) {
            List<Expression> list = new LinkedList<>();
            Token listLiteralToken = tokens.consumeToken(); // consume the '['
            // while loop until match the right brace
            while (!tokens.match(RIGHT_BRACKET)) {
                if (tokens.match(EOF)) {
                    ListLiteralExpression listLiteralExpression = new ListLiteralExpression(list);
                    listLiteralExpression.addError(ErrorType.UNTERMINATED_LIST);
                    return listLiteralExpression;
                }
                Expression e = parseExpression();
                list.add(e);
                if (!tokens.match(RIGHT_BRACKET) && !tokens.match(EOF)) {
                    tokens.consumeToken(); // consume the ','
                }
            }
            tokens.consumeToken();
            ListLiteralExpression listLiteralExpression = new ListLiteralExpression(list);
            return listLiteralExpression;
        } else {
            SyntaxErrorExpression syntaxErrorExpression = new SyntaxErrorExpression(tokens.consumeToken());
            return syntaxErrorExpression;
        }
    }

    //============================================================
    //  Parse Helpers
    //============================================================
    private Token require(TokenType type, ParseElement elt) {
        return require(type, elt, ErrorType.UNEXPECTED_TOKEN);
    }

    private Token require(TokenType type, ParseElement elt, ErrorType msg) {
        if(tokens.match(type)){
            return tokens.consumeToken();
        } else {
            elt.addError(msg, tokens.getCurrentToken());
            return tokens.getCurrentToken();
        }
    }

}
