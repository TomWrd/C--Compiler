To run the program pass the test file path as the argument,
if test file is saved in same folder pass file name.

Program will return accept or rejct based on if the source code is
valid. 



Project 1(Tokenizer)
--------------------
SPECIFICATIONS:
Textbook: Compiler Construction Principles - Kenneth C. Louden
Your project is to use the grammar definition in the appendix
of your text to guide the construction of a lexical analyzer. 
The lexical analyzer should return tokens as described. Keep 
in mind these tokens will serve as the input to the parser.
You must enhance the definitions by adding a keyword "float"
as a data type to the material on page 493 and beyond.
Specifically, rule 5 on page 492 should state

    type-specifier -> int | void | float

and any other modifications necessary must be included. 

Page 491 and 492 should be used to guide the construction of the
lexical analyzer. A few notable features:
0) the project's general goal is to construct a list of tokens capable
   of being passed to a parser.
1) comments should be totally ignored, not passed to the parser and
   not reported.
2) comments might be nested.
3) one line comments are designated by //
4) multiple line comments are designated by /* followed by */ in 
   a match up fashion for the nesting.
5) a symbol table* for identifiers should be constructed (as
   per recommendation of your text, I actually recommend
   construction of the symbol table during parsing).
   a) the symbol table should keep track of the identifier
   b) be extensible
   c) keep track of scope
   d) be constructed efficiently
   * this will not be evaluated until project 3
6) upon reporting of identifiers, their nesting depth/declarations
   should be displayed.

Sample input:
/**/          /*/* */   */
/*/*/****This**********/*/    */
/**************/
/*************************
i = 333;        ******************/       */

iiii = 3@33;

int g 4 cd (int u, int v)      {
if(v == >= 0) return/*a comment*/ u;
else ret_urn gcd(vxxxxxxvvvvv, u-u/v*v);
       /* u-u/v*v == u mod v*/
!   
}

return void while       void main()


Sample output:
INPUT: /**/          /*/* */   */
INPUT: /*/*/****This**********/*/    */
INPUT: /**************/
INPUT: /*************************
INPUT: i = 333;        ******************/       */
*  
/  
INPUT: iiii = 3@33;
ID: iiii 
=
NUM: 3
Error: @33
;

INPUT: int g 4 cd (int u, int v)      {
keyword: int
ID: g
NUM: 4
ID: cd
(
keyword: int
ID: u
,
keyword: int
ID: v
)
{

INPUT: if(v == >= 0) return/*a comment*/ u;
keyword: if
(
ID: v
==
>=
NUM: 0
)
keyword: return
ID: u
;

INPUT: else ret_urn gcd(vxxxxxxvvvvv, u-u/v*v);
keyword: else
ID: ret
Error: _urn
ID: gcd
(
ID: vxxxxxxvvvvv
,
ID: u
-
ID: u
/
ID: v
*
 ID: v
)
;
INPUT: /* u-u/v*v == u mod v*/

INPUT: !   
Error: !
INPUT: }
}


INPUT: return void while       void main()
keyword: return
keyword: void
keyword: while
keyword: void
ID: main
(
)

Project 2(Parser)
-----------------
SPECIFICATION:
Your project is to use the grammar definition in the appendix
of your text to guide the construction of a recursive descent parser.
The parser should follow the grammar as described in A.2 page 492.

You should enhance the grammar to include FLOAT as
appropriate throughout all the grammar rules.

Upon execution, your project should report 

ACCEPT

or 

REJECT

exactly. Failure to print ACCEPT or REJECT appropriately will
result penalty for the test file. 


Project 3(Semantic Analyzer)
----------------------------
SPECIFICATION:
Project 3 is the construction of semantic analyzer. You are to include 
in your parser appropriate checking not included in the grammar, but 
defined by the language.

This is going to be the test of the quality of your symbol
table implemented during parsing. You are to determine and
implement appropriate checks as discussed.

This project must be complete in that the lexical analyzer must be
included to create the tokens required by the parser and
semantic analyzer.

Your project should report on a single line without any additional
characters

ACCEPT

or 

REJECT

upon completion of the analysis.

--------------------------

The following represents a set of tests that might be considered. The
list is not required, nor is it complete, but may be used as a goal
for semantic analysis.

functions declared int or float  must have a return value of the
   correct type.
void functions may or may not have a return, but must not return a
   value.
parameters and arguments agree in number
parameters and arguments agree in type
operand agreement
operand/operator agreement
array index agreement
variable declaration (all variables must be declared ... scope)
variable declaration (all variables declared once ... scope)
void functions cannot have a return value
each program must have one main function
return only simple structures
id's should not be type void
each function should be defined (actually a linker error)