package org.baratinage.utils.equation;

public class Lexer {
  private final String input;
  private int pos = 0;

  Lexer(String input) {
    this.input = input;
  }

  Token next() {
    while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
      pos++;
    }

    if (pos >= input.length())
      return new Token(TokenType.EOF, "");

    char c = input.charAt(pos);

    // Numbers
    // if (Character.isDigit(c) || c == '.') {
    // int start = pos;
    // while (pos < input.length() &&
    // (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
    // pos++;
    // }
    // return new Token(TokenType.NUMBER, input.substring(start, pos));
    // }
    if (Character.isDigit(c) || c == '.') {
      int start = pos;
      boolean seenDot = false;
      boolean seenExp = false;

      // Integer / decimal part
      while (pos < input.length()) {
        char ch = input.charAt(pos);

        if (Character.isDigit(ch)) {
          pos++;
        } else if (ch == '.' && !seenDot && !seenExp) {
          seenDot = true;
          pos++;
        } else {
          break;
        }
      }

      // Exponent part
      if (pos < input.length()) {
        char ch = input.charAt(pos);
        if ((ch == 'e' || ch == 'E')) {
          seenExp = true;
          pos++;

          // Optional sign
          if (pos < input.length()) {
            ch = input.charAt(pos);
            if (ch == '+' || ch == '-') {
              pos++;
            }
          }

          // Must have digits in exponent
          int expStart = pos;
          while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            pos++;
          }

          if (expStart == pos) {
            throw new RuntimeException("Invalid scientific notation");
          }
        }
      }

      return new Token(TokenType.NUMBER, input.substring(start, pos));
    }

    // Identifiers (variables / functions)
    if (Character.isLetter(c)) {
      int start = pos;
      while (pos < input.length() &&
          (Character.isLetterOrDigit(input.charAt(pos)) || input.charAt(pos) == '_')) {
        pos++;
      }
      return new Token(TokenType.IDENTIFIER, input.substring(start, pos));
    }

    pos++;
    return switch (c) {
      case '=' -> new Token(TokenType.EQUAL, "=");
      case '+' -> new Token(TokenType.PLUS, "+");
      case '-' -> new Token(TokenType.MINUS, "-");
      case '*' -> new Token(TokenType.STAR, "*");
      case '/' -> new Token(TokenType.SLASH, "/");
      case '^' -> new Token(TokenType.CARET, "^");
      case '(' -> new Token(TokenType.LPAREN, "(");
      case ')' -> new Token(TokenType.RPAREN, ")");
      case ',' -> new Token(TokenType.COMMA, ",");
      default -> throw new RuntimeException("Unexpected char: " + c);
    };
  }
}
