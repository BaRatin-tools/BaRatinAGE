package org.baratinage.utils.equation;

import java.util.ArrayList;
import java.util.List;

public class EquationParser {

  private final Lexer lexer;
  private Token current;

  public EquationParser(String input) {
    lexer = new Lexer(input);
    current = lexer.next();
  }

  private void consume(TokenType type) {
    if (current.type != type)
      throw new RuntimeException("Expected " + type + " but got " + current.type);
    current = lexer.next();
  }

  // public Expr parse() {
  // return parseExpression(0);
  // }

  public Expr parse() {
    Expr left = parseExpression(0);

    if (current.type == TokenType.EQUAL) {
      consume(TokenType.EQUAL);
      Expr right = parseExpression(0);
      return new EquationExpr(left, right);
    }

    return left;
  }

  private Expr parseExpression(int precedence) {
    Expr left = parsePrimary();

    while (current.type.precedence() > precedence) {
      Token op = current;
      consume(current.type);
      int nextPrecedence = op.type.precedence();
      if (op.type == TokenType.CARET) {
        nextPrecedence--; // right-associative
      }
      Expr right = parseExpression(nextPrecedence);
      left = new BinaryExpr(op.text, left, right);
    }
    return left;
  }

  private Expr parsePrimary() {
    Token tok = current;

    switch (tok.type) {
      case NUMBER -> {
        consume(TokenType.NUMBER);
        return new NumberExpr(Double.parseDouble(tok.text));
      }

      case IDENTIFIER -> {
        consume(TokenType.IDENTIFIER);

        // Function call
        if (current.type == TokenType.LPAREN) {
          consume(TokenType.LPAREN);
          List<Expr> args = new ArrayList<>();

          if (current.type != TokenType.RPAREN) {
            do {
              args.add(parse());
              if (current.type == TokenType.COMMA)
                consume(TokenType.COMMA);
            } while (current.type != TokenType.RPAREN);
          }

          consume(TokenType.RPAREN);
          return new FunctionExpr(tok.text, args);
        }

        return new VariableExpr(tok.text);
      }

      case MINUS -> {
        consume(TokenType.MINUS);
        return new UnaryExpr("-", parsePrimary());
      }

      case LPAREN -> {
        consume(TokenType.LPAREN);
        Expr expr = parse();
        consume(TokenType.RPAREN);
        return expr;
      }

      default -> throw new RuntimeException("Unexpected token: " + tok.type);
    }

  }

}
