package org.baratinage.utils.equation;

public enum TokenType {
  EQUAL,
  NUMBER, IDENTIFIER,
  PLUS, MINUS, STAR, SLASH, CARET,
  LPAREN, RPAREN, COMMA,
  EOF;

  public int precedence() {
    return switch (this) {
      case PLUS, MINUS -> 1;
      case STAR, SLASH -> 2;
      case CARET -> 3;
      default -> 0;
    };
  }
}
