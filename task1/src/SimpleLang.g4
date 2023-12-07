grammar SimpleLang;

prog : dec+ EOF;

dec
    : typed_idfr  LParen vardec? RParen body // change vardec and args
;
vardec:  typed_idfr  (Comma typed_idfr)*

;
typed_idfr
    : type Idfr
;

type
    : IntType | BoolType | UnitType
;

body
    : LBrace (typed_assign ';')* ene RBrace
;
ene
    :exp (Semicolon exp)*

;
block
    : LBrace  ene RBrace
;
typed_assign : typed_idfr Assign exp;

exp
    : Idfr Assign exp                                 #AssignExpr
    | LParen exp binop exp RParen                           #BinOpExpr
    | Idfr LParen args RParen                              #InvokeExpr
    | block                                                 #BlockExpr
    | If exp Then block Else block                          #IfExpr
    | While exp Do block                                    #WhileExpr
    |Repeat block Until exp                                 #Repeat
    | Print exp                                             #PrintExpr
    | Space                                                 #SpaceExpr
    | Skip                                                  #Skip
    | NewLine                                               #NewLine
    | Idfr                                                  #IdExpr
    | IntLit                                                #IntExpr
    |BoolLit                                                #BoolLit

;
args:exp (Comma exp)*;//was prevooiusly args:(exp (Comma exp)*)?
binop
    : Eq              #EqBinop
    | Less            #LessBinop
    | LessEq          #LessEqBinop
    | Great           #GreatBinop
    | GreatEq         #GreatEqBinop
    | Plus            #PlusBinop
    | Minus           #MinusBinop
    | Times           #TimesBinop
    | Divide          #DivideBinop
    |And              #AndBinop
    |Or               #OrBinop
    |Xor             #XorBinop
;

LParen : '(' ;
Comma : ',' ;
RParen : ')' ;
LBrace : '{' ;
Semicolon : ';' ;
RBrace : '}' ;

Eq : '==' ;
Less : '<' ;
LessEq : '<=' ;
Great :'>';
GreatEq :'>=';




Plus : '+' ;
Times : '*' ;
Minus : '-' ;
Divide :'/';
And :'&';
Or :'|';
Xor :'^';


Assign : ':=' ;

Print : 'print' ;
Space : 'space' ;
NewLine : 'newline' ;
If : 'if' ;
Then : 'then' ;
Else : 'else' ;
While :'while';
Do :'do';
Repeat :'repeat';
Until : 'until';
Skip  : 'skip';



IntType : 'int' ;
BoolType : 'bool' ;
UnitType : 'unit' ;

BoolLit : 'true' | 'false' ;
IntLit : '0' | ('-'? [1-9][0-9]*) ;
Idfr : [a-z][A-Za-z0-9_]* ;
WS : [ \n\r\t]+ -> skip ;

