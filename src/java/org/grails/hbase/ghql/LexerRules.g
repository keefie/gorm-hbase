header {
package org.grails.hbase.ghql;
}

// @@scanner
//----------------------------------------------------------------------------
// The Gorm HBase Query Language 
//----------------------------------------------------------------------------
class ParserRules extends Parser;
options { buildAST=true; }

  nest_expr : LPAREN^ comp_expr RPAREN! ;
  comp_expr : expr (logical expr)+ ;
  expr      : PROPERTY_NAME (WS)+ COMPARISON_OPERATOR (WS)+ value ;
  value     : BYTE|NUMBER|STRING|BOOLEAN|DATE|TIMELONG|TIMESHORT|TIMESTAMPLONG|TIMESTAMPSHORT ;
  logical   : AND|OR ;

class LexerRules extends Lexer;

  options {
    charVocabulary = '\0'..'\377';
    testLiterals=false;    // don't automatically test for literals
    k=2;                  // two characters of lookahead
  }

  // @@startrules

  // Whitespace -- ignored
  WS
    : ( ' '
        | '\t'
        | '\f'
        |  "\r\n"  // DOS/Windows
        | '\r'    // Macintosh
        | '\n'    // Unix
      )
      { $setType(Token.SKIP); }
    ;

  NUMBER   : ('-')?('0'..'9')+ ( ('.') ('0'..'9')+ )? ;
  BYTE     : ('0')('x')(('0'..'9')|('A'..'F'))(('0'..'9')|('A'..'F'))+ ;
  DATE     : '\''!
                ('0'..'9')('0'..'9')('0'..'9')('0'..'9')('-')('0'..'9')('0'..'9')('-')('0'..'9')('0'..'9')
                (
                   (
                       (' ')('0'..'9')('0'..'9')(':')('0'..'9')('0'..'9')(':')('0'..'9')('0'..'9')
                       (
                            ('.')('0'..'9')('0'..'9')('0'..'9')
                            |
                       )
                   )
                   |
                )
            '\''! ;

  STRING   : '\''!
         (
             ~('\''|'\n'|'\r')
         )*
         ( '\''!
         |
         )
         ;

  PROPERTY_NAME    : ('a'..'z')('a'..'z'|'A'..'Z'|'0'..'9' | '-' | '_')*
                   | ('e'|'E')('q'|'Q') { $setType(EQUAL); }
                   | ('n'|'N')('e'|'E') { $setType(NOT_EQUAL); }
                   | ('l'|'L')('t'|'T') { $setType(LT); }
                   | ('l'|'L')('e'|'E') { $setType(LE); }
                   | ('g'|'G')('t'|'T') { $setType(GT); }
                   | ('g'|'G')('e'|'E') { $setType(GE); }
                   | ('a'|'A')('n'|'N')('d'|'D') { $setType(AND); }
                   | ('o'|'O')('r'|'R') { $setType(OR); }
                   | ('t'|'T')('r'|'R')('u'|'U')('e'|'E') { $setType(BOOLEAN); }
                   | ('f'|'F')('a'|'A')('l'|'L')('s'|'S')('e'|'E') { $setType(BOOLEAN); }
                   ;

  // Operators
  COMPARISON_OPERATOR  : ( EQUAL | NOT_EQUAL | LT | LE | GT | GE | SUBSTITUTE) ;


  protected SUBSTITUTE : ":="  ;
  protected EQUAL      : "="   ;
  protected NOT_EQUAL  : "!="  ;
  protected LT         : "<"   ;
  protected LE         : "<="  ;
  protected GT         : ">"   ;
  protected GE         : ">="  ;

  LPAREN     : '('   ;
  RPAREN     : ')'   ;

// @@endrules

// @@endscanner
