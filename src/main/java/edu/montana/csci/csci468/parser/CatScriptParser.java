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
            //todo() I mutate the '{' into '}' and the '}' into ']'
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
        if (tokens.match(VAR)) {
            Statement varStmt = parseVariableStatement();
            return varStmt;
        }
        if (tokens.match(IF)) {
            Statement ifStmt = parseIfStatement();
            return ifStmt;
        }
        if (tokens.match(FOR)) {
            Statement forStmt = parseForStatement();
            return forStmt;
        }
        else if (tokens.match(PRINT)) {
            Statement printStmt = parsePrintStatement();
            if (printStmt != null) {
                return printStmt;
            }
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

    private Statement parseForStatement() {
        if (tokens.match(FOR)) {
            ForStatement forStatement = new ForStatement();
            forStatement.setStart(tokens.consumeToken());
            require(LEFT_PAREN, forStatement);
            IdentifierExpression id = (IdentifierExpression) parseExpression(); // REFAC: what's the best way to do this?
            forStatement.setVariableName(id.getName());
            require(IN,forStatement);
            Expression expression = parseExpression();
            forStatement.setExpression(expression);
            require(RIGHT_PAREN, forStatement);
            require(LEFT_BRACE, forStatement);
            // add the statements to the for loop
            List<Statement> statement_list = new LinkedList<>();
            while (!tokens.match(RIGHT_BRACE) && !tokens.match(EOF)) { // REFAC: is EOF the best way of doing this ?
                statement_list.add(parseProgramStatement());
            }
            forStatement.setBody(statement_list);
            forStatement.setEnd(require(RIGHT_BRACE, forStatement));
            return forStatement;
        } else {
            return new SyntaxErrorStatement(tokens.consumeToken());
        }
    }

    private Statement parseIfStatement() {
        if (tokens.match(IF)) {
            IfStatement ifStatement = new IfStatement();
            ifStatement.setStart(tokens.consumeToken());
            require(LEFT_PAREN, ifStatement);
            Expression ifExpression = parseExpression();
            ifStatement.setExpression(ifExpression);
            require(RIGHT_PAREN, ifStatement);
            require(LEFT_BRACE, ifStatement);
            List<Statement> trueStatementsList= new LinkedList<>();
            while (!tokens.match(RIGHT_BRACE) && !tokens.match(EOF)) {
                trueStatementsList.add(parseProgramStatement());
            }
            ifStatement.setTrueStatements(trueStatementsList);
            require(RIGHT_BRACE, ifStatement);

            if (tokens.match(ELSE)) {
                tokens.consumeToken(); // consume the else
                if (tokens.match(IF)) {
                    parseIfStatement();
                } else {
                    require(LEFT_BRACE, ifStatement);
                    List<Statement> elseStatementsList = new LinkedList<>();
                    while (!tokens.match(RIGHT_BRACE) && !tokens.match(EOF)) {
                        elseStatementsList.add(parseProgramStatement());
                    }
                    ifStatement.setElseStatements(elseStatementsList);
                    ifStatement.setEnd(require(RIGHT_BRACE, ifStatement));
                }
            }
            return ifStatement;
        } else {
            return new SyntaxErrorStatement(tokens.consumeToken());
        }
    }

    private Statement parseVariableStatement() {
        if (tokens.match(VAR)) {
            VariableStatement varStatement = new VariableStatement();
            varStatement.setStart(tokens.consumeToken());
            IdentifierExpression id = (IdentifierExpression) parseExpression();
            varStatement.setVariableName(id.getName());
            if (tokens.match(COLON)) {
                tokens.consumeToken(); // consume the colon
                varStatement.setExplicitType(parseTypeExpression());
            }
            require(EQUAL, varStatement);
            varStatement.setExpression(parseExpression());

            return varStatement;
        } else {
            return new SyntaxErrorStatement(tokens.consumeToken());
        }
    }


    //============================================================
    //  Expressions
    //============================================================

    private Expression parseExpression() {
        return parseEqualityExpression();
    }

    private FunctionCallExpression parseFunctionCallExpression(Token identifierToken) {
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
    }

    private Expression parseEqualityExpression() {
        Expression expression = parseComparisonExpression();
        while (tokens.match(EQUAL_EQUAL, BANG_EQUAL)) {
            Token operator = tokens.consumeToken();
            Expression rightHandSide = parseComparisonExpression();
            EqualityExpression equalityExpression = new EqualityExpression(operator, expression, rightHandSide);
            equalityExpression.setStart(expression.getStart());
            equalityExpression.setEnd(rightHandSide.getEnd());
            expression = equalityExpression;
        }
        return expression;
    }

    private Expression parseComparisonExpression() {
        Expression expression = parseAdditiveExpression();
        while (tokens.match(LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)) {
            Token operator = tokens.consumeToken();
            Expression rightHandSide = parseAdditiveExpression();
            ComparisonExpression comparisonExpression = new ComparisonExpression(operator, expression, rightHandSide);
            comparisonExpression.setStart(expression.getStart());
            comparisonExpression.setEnd(rightHandSide.getEnd());
            expression = comparisonExpression;
        }

        return expression;
    }

    private Expression parseAdditiveExpression() {
        Expression expression = parseFactorExpression();
        Expression rightHandSide;
        while (tokens.match(PLUS, MINUS)) {
            Token operator = tokens.consumeToken();
            rightHandSide = parseFactorExpression();
//            if (tokens.match(LEFT_PAREN)) {
//                tokens.consumeToken();
//                rightHandSide = new ParenthesizedExpression(parseFactorExpression());
//                tokens.consumeToken(); // consume right paren
//            } else {
//                rightHandSide = parseUnaryExpression();
//            }
            AdditiveExpression additiveExpression = new AdditiveExpression(operator, expression, rightHandSide);
            additiveExpression.setStart(expression.getStart());
            additiveExpression.setEnd(rightHandSide.getEnd());
            expression = additiveExpression;
        }

        return expression;
    }

    private Expression parseFactorExpression() {
        Expression expression = parseUnaryExpression();
        Expression rightHandSide;
        while (tokens.match(STAR, SLASH)) {
            Token operator = tokens.consumeToken();
//            if (tokens.match(LEFT_PAREN)) {
//                tokens.consumeToken();
//                rightHandSide = new ParenthesizedExpression(parseAdditiveExpression());
//                tokens.consumeToken(); // consume right paren
//            } else {
//                rightHandSide = parseUnaryExpression();
//            }
            rightHandSide = parseUnaryExpression();
            FactorExpression factorExpression = new FactorExpression(operator, expression, rightHandSide);
            factorExpression.setStart(expression.getStart());
            factorExpression.setEnd(rightHandSide.getEnd());
            expression = factorExpression;
        }
        return expression;
    }

    private Expression parseListLiteralExpression() {
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
                FunctionCallExpression functionCallExpression = parseFunctionCallExpression(identifierToken);
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
            return parseListLiteralExpression();
        } else if(tokens.match(LEFT_PAREN)) {
            Token left_paren_token = tokens.consumeToken();
            ParenthesizedExpression parenthesizedExpression = new ParenthesizedExpression(parseExpression());
            parenthesizedExpression.setToken(left_paren_token);
            return parenthesizedExpression;
        }
        else {
            SyntaxErrorExpression syntaxErrorExpression = new SyntaxErrorExpression(tokens.consumeToken());
            return syntaxErrorExpression;
        }
    }

    // RFC: is there a better way of doing this?
    private CatscriptType parseTypeExpression() {
        Token type = tokens.consumeToken();
        if (type.getStringValue().equals("int")) {
            return CatscriptType.INT;
        }
        else {
            return CatscriptType.NULL; // RFC: change this later
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
