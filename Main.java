package PL112_11027113;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;

class Done extends Exception {
  Done() {
    super( "Our-C exited ..." );
  } // Done()
  
  Done( String message ) {
    super( message );
  } // Done()
  
} // class Done

class Lexical_error extends Exception {
  Lexical_error( char c, int line ) {
    super( "> Line " + line + " : unrecognized token with first char '" + c + "'" );
    MyScanner.stokenBuffer.clear();   
    MyParser.scorrtoken.clear();
    MyParser.stempvar.clear();
  } // Lexical_error()

  Lexical_error( String message ) {
    super( message );
  } // Lexical_error()
} // class Lexical_error

class Syntax_error extends Exception {
  Syntax_error( MyToken token ) {
    super( "> Line " + token.mline + " : unexpected token '" +  token.mname + "'" );
    MyScanner.stokenBuffer.clear();
    MyParser.scorrtoken.clear();
    MyParser.stempvar.clear();
  } // Syntax_error()

  Syntax_error( String message ) {
    super( message );
  } // Syntax_error()


} // class Syntax_error

class Semantic_error extends Exception {
  Semantic_error( MyToken token ) {
    super( "> Line " + token.mline + " : undefined identifier '" + token.mname + "'" );
    MyScanner.stokenBuffer.clear();
    MyParser.scorrtoken.clear();
    MyParser.stempvar.clear();
  } // Semantic_error()

  Semantic_error( String message ) {
    super( message );
  } // Semantic_error()

} // class Semantic_error

class MyToken {
  String mname; // token name
  String mtype; // token type for parser
  int mline;
  ArrayList<String> mlist; // token value []
  int mlength; // array size
  boolean mcond; // is true or false?
  boolean mdot; // is float? 
  boolean mfunc; // is function?
  String mrtype; // token real type   ex. int float 
  int mindex;

  MyToken() { 
    this.mlength = 1;
    this.mindex = 0;
    this.mlist = new ArrayList<String>();
    this.mname = ""; // token name
    this.mtype = ""; // token type for parser
    int mline = 1;
    this.mcond = false; // is true or false?
    this.mdot = false; // is float? 
    this.mfunc = false; // is function?
    this.mrtype = ""; // token real type   ex. int float 
  } // MyToken()
  
  MyToken( MyToken token ) {
    this.mname = new String( token.mname );
    this.mtype = new String( token.mtype );
    this.mline = token.mline;
    this.mdot = token.mdot;
    this.mrtype = new String( token.mrtype ); 
    this.mfunc = token.mfunc;
    this.mlength = token.mlength;
    this.mlist = new ArrayList<String>();
    for ( int i = 0; i < token.mlist.size() ; i++ ) {
      this.mlist.add( new String( token.mlist.get( i ) ) );
    } // for
    
    this.mindex = token.mindex;
    this.mcond = token.mcond;
  } // MyToken()

} // class MyToken

class MyScanner {
  static BufferedReader sreader = new BufferedReader( new InputStreamReader( System.in ) );
  static ArrayList<MyToken> stokenBuffer = new ArrayList<MyToken>();
  static int sline = 1;
  

  Boolean IsDigit( char c ) {
    return Character.isDigit( c );
  } // IsDigit()

  Boolean IsAlphabet( char c ) {
    return Character.isLetter( c );
  } // IsAlphabet()

  Boolean IsSpecial( char c ) {  
    if ( c == '=' || c == '>' || c == '<' || c == ':' || c == ';' || c == '+' || c == '-' ||
         c == '*' || c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}' ||
         c == '%' || c == '^' || c == '!' || c == '&' || c == '|' || c == '?' || c == ',' ) 
    {   // without _ . / ' " 
      return true;
    } // if

    return false;
  } // IsSpecial()

  char Peek() throws Exception {
    sreader.mark( 1 );
    char c = ( char ) sreader.read();
    sreader.reset();


    if ( c == ( char ) -1 ) {
      throw new Exception( );
    } // if

    return c;
  } // Peek()
  
  char Get() throws Exception {
    char c = ( char ) sreader.read();
    return c;
  } // Get()
  
  void SkipWS() throws Exception {
    char c = Peek();
    while ( c == ' ' || c == '\t' || c == '\n' || c == '\r' ) {
      if ( c == '\n' ) {
        sline += 1;
      } // if

      c = Get();
      c = Peek();
    } // while 
    
  } // SkipWS()
  

  MyToken GetToken() throws Exception {
    MyToken token = new MyToken();
    StringBuilder content = new StringBuilder(); 

    if ( !stokenBuffer.isEmpty() ) {
      return stokenBuffer.remove( stokenBuffer.size() - 1 );
    } // if 

    SkipWS();
    char c = Peek(); // only peek not Get

    if ( IsAlphabet( c ) ||  c == '_' ) { // Is Identifier 
      while ( IsAlphabet( c ) ||  c == '_' || IsDigit( c ) ) {
        content.append( Get() );
        c = Peek();
      } // while 

      String temp = content.toString();
      if ( temp.equals( "int" ) || temp.equals( "float" )  ||
           temp.equals( "char" ) || temp.equals( "bool" ) ||
           temp.equals( "string" ) || temp.equals( "void" ) ||
           temp.equals( "if" ) || temp.equals( "else" ) ||
           temp.equals( "while" ) || temp.equals( "do" ) ||
           temp.equals( "return" ) ) {
        token.mtype = temp.toUpperCase();
        token.mrtype = temp;
      } // if
      else if ( temp.equals( "true" ) || temp.equals( "false" ) ) {
        token.mtype = "CONSTANT";
        token.mrtype = temp.toUpperCase();
      } // else if
      else 
        token.mtype = "IDENTIFIER";

      token.mline = sline;
      token.mname = content.toString();
    } // if 
    else if ( IsDigit( c ) || c == '.' ) { // Is constant of Number 
      boolean firstdot = false;
      boolean isdot = false;
      while ( ( IsDigit( c ) || c == '.' ) ) {
        if ( c == '.' && !firstdot ) { // first dot
          firstdot = true;
          isdot = true;
          content.append( Get() );
          c = Peek();
        } // if 
        else if ( IsDigit( c ) ) {
          content.append( Get() );
          c =  Peek();
        } // else if
        else {
          LastTokens();
          throw new Lexical_error( c, sline );
        } // else 
      } // while 

      token.mline = sline;
      token.mname = content.toString();
      token.mtype = "CONSTANT";
      if ( isdot ) {
        token.mrtype = "FLOAT";
      } // if
      else {
        token.mrtype = "INT";
      } // else

      token.mdot = isdot;
    } // else if 
    else if ( c == '\'' || c == '"' ) { // Is constnat of string
      boolean ischar = false;
      char quote = c;
      Get(); // ' "
      c = Peek();
      if ( quote == '\'' ) {
        content.append( Get() );
        ischar = true;
        c = Peek();
        if ( c != '\'' ) {
          LastTokens();
          throw new Lexical_error( '\'', sline );
        } // if
      } // if

      while ( c != quote ) {
        if ( c == '\\' ) { // escape character
          content.append( c );
          content.append( Get() );
        } // if 
        else {
          content.append( Get() );
        } // else 

        c = Peek();
      } // while 

      Get();
      token.mname = content.toString();
      token.mtype = "CONSTANT";
      if ( ischar ) {
        token.mrtype = "CHAR";
      } // if
      else {
        token.mrtype = "STRING";
      } // else

      token.mline = sline;
    } // else if 
    else if ( IsSpecial( c ) ) { // Is Symbol
      content.append( Get() );
      c = Peek();
      if ( content.charAt( 0 ) == '>' && ( c == '>' || c == '=' ) ) {  // > >= >>
        content.append( Get() );
      } // if
      else if ( content.charAt( 0 ) == '<' && ( c == '<' || c == '=' ) ) {  // < <= <<
        content.append( Get() );
      } // else if
      else if ( ( content.charAt( 0 ) == '+' || content.charAt( 0 ) == '-' ||
                  content.charAt( 0 ) == '=' || content.charAt( 0 ) == '!' || 
                  content.charAt( 0 ) == '*' || content.charAt( 0 ) == '%' || 
                  content.charAt( 0 ) == '&' || content.charAt( 0 ) == '|' ) 
                && ( c == '=' || ( c == content.charAt( 0 ) && c != '!' ) ) ) 
      {  // Handle +=, -=, ++, --, ==, !=, *=, %=, &&, ||
        content.append( Get() );
      } // else if
      
      String temp = content.toString();
      token.mname = temp;
      token.mline = sline;
      if ( temp.equals( "(" ) ) token.mtype = "(";
      else if ( temp.equals( ")" ) ) token.mtype = ")";
      else if ( temp.equals( "[" ) ) token.mtype = "[";
      else if ( temp.equals( "]" ) ) token.mtype = "]";
      else if ( temp.equals(  "{" ) ) token.mtype = "{";
      else if ( temp.equals( "}" ) ) token.mtype = "}";
      else if ( temp.equals( "+" ) ) token.mtype = "+";
      else if ( temp.equals( "-" ) ) token.mtype = "-";
      else if ( temp.equals( "*" ) ) token.mtype = "*";
      else if ( temp.equals( "%" ) ) token.mtype = "%"; 
      else if ( temp.equals( "^" ) ) token.mtype = "^";
      else if ( temp.equals(  "<" ) ) token.mtype = "<"; 
      else if ( temp.equals( ">" ) ) token.mtype = ">";
      else if ( temp.equals( ">=" ) ) token.mtype = "GE";
      else if ( temp.equals( "<=" ) ) token.mtype = "LE";
      else if ( temp.equals( "==" ) ) token.mtype = "EQ";
      else if ( temp.equals( "!=" ) ) token.mtype = "NEQ";
      else if ( temp.equals( "&" ) ) token.mtype = "&";
      else if ( temp.equals( "|" ) ) token.mtype = "|";
      else if ( temp.equals( "=" ) ) token.mtype = "=";
      else if ( temp.equals( "!" ) ) token.mtype = "!";
      else if ( temp.equals( "&&" ) ) token.mtype = "AND";
      else if ( temp.equals( "||" ) ) token.mtype = "OR";
      else if ( temp.equals( "+=" ) ) token.mtype = "PE";
      else if ( temp.equals( "-=" ) ) token.mtype = "ME";
      else if ( temp.equals( "*=" ) ) token.mtype = "TE";
      else if ( temp.equals( "%=" ) ) token.mtype = "RE";
      else if ( temp.equals( "++" ) ) token.mtype = "PP";
      else if ( temp.equals( "--" ) ) token.mtype = "MM";
      else if ( temp.equals( ">>" ) ) token.mtype = "RS";
      else if ( temp.equals( "<<" ) ) token.mtype = "LS";
      else if ( temp.equals( ";" ) ) token.mtype = ";";
      else if ( temp.equals( "," ) ) token.mtype = ",";
      else if ( temp.equals( "?" ) ) token.mtype = "?";
      else if ( temp.equals( ":" ) ) token.mtype = ":";
    } // else if
    else if ( c == '/' ) { // comment or division
      content.append( Get() );
      c = Peek();
      if ( c == '=' ) {
        content.append( Get() );
        token.mline = sline;
        token.mname = "/=";
        token.mtype = "DE";
      } // if
      else if ( c == '/' ) {  // single line comment
        while ( c != '\n' ) {
          c = Get();
          c = Peek();
        } // while
        

        return GetToken();
      } // else if
      else {
        token.mline = sline;
        token.mname = "/";
        token.mtype = "/";
      } // else

    } // else if 
    else { // error 
      LastTokens();
      throw new Lexical_error( c, sline );
    } // else 

    // System.out.println( "GT: " + token.mname + " " + token.mtype );
    return token;
  } // GetToken()

  MyToken PeekToken() throws Exception {
    MyToken token = new MyToken();
    token = GetToken();
    stokenBuffer.add( 0, token );
    return token;
  } // PeekToken()

  void LastTokens() throws Exception {
    char trash = ' ';
    while ( trash != '\n' ) {
      trash = Get();
    }  // while 

  } // LastTokens()

} // class MyScanner

class MyParser {
  static MyScanner sscanner = new MyScanner();
  
  // all correct tokens
  static ArrayList<MyToken> scorrtoken = new ArrayList<MyToken>();
  static String svartype;
  static ArrayList<ArrayList<MyToken> > stempvar = new ArrayList<ArrayList<MyToken> >();
  // all Variable
  
  // static ArrayList<ArrayList<MyToken> > stable = new ArrayList<ArrayList<MyToken> >(); 
  

  boolean Isdefined( MyToken token ) {
    
    if ( token.mname.equals( "Done" ) || token.mname.equals( "cout" ) ||
         token.mname.equals( "cin" ) ) {
      return true;
    } // if
    
    for ( int i = 0; i < stempvar.size() ; i++ ) {
      for ( int x = 0; x < stempvar.get( i ).size() ; x++ ) {
        if ( token.mname.equals( stempvar.get( i ).get( x ).mname ) ) {
          return true;
        } // if
        
      } // for 
      
    } // for

    for ( int i = 0; i < MyExecute.stable.size() ; i++ ) {
      for ( int j = 0; j < MyExecute.stable.get( i ).size() ; j++ ) {
        if ( token.mname.equals( MyExecute.stable.get( i ).get( j ).mname ) ) {
          return true;
        } // if

      } // for

    } // for

    return false;
  } // Isdefined()

  void User_input() throws Exception {
//    System.out.println("---------User_input--------");

    MyToken token = new MyToken();
    token = sscanner.PeekToken();

    if ( token.mtype.equals( "VOID" ) || token.mtype.equals( "INT" ) ||
         token.mtype.equals( "CHAR" ) || token.mtype.equals( "STRING" )  ||
         token.mtype.equals( "BOOL" ) || token.mtype.equals( "FLOAT" ) ) {
      Definition();
    } // if 
    else if ( token.mtype.equals( ";" ) || token.mtype.equals( "RETURN" ) ||
              token.mtype.equals( "IF" ) || token.mtype.equals( "WHILE" ) ||
              token.mtype.equals( "DO" ) || token.mtype.equals( "{" ) || 
              token.mtype.equals( "IDENTIFIER" ) || token.mtype.equals( "PP" ) ||
              token.mtype.equals( "MM" ) ||  token.mtype.equals( "+" ) ||
              token.mtype.equals( "-" ) || token.mtype.equals( "!" ) ||
              token.mtype.equals( "CONSTANT" ) || token.mtype.equals( "(" ) ) { 
      Statement();
    } // else if 
    else {
      sscanner.LastTokens();
      throw new Syntax_error( token );
    } // else 
    
    char c = sscanner.Peek();
    if ( c == '\n' ) {
      c = sscanner.Get();
    } // if       
    else if ( c == '\r' ) {
      c = sscanner.Get();
      c = sscanner.Get();
    } // else if 
    else { 
      sscanner.SkipWS();
      c = sscanner.Peek();
      if ( c == '/' ) {
        c = sscanner.Get();
        c = sscanner.Peek();
        if ( c == '/' ) {
          while ( c != '\n' ) {
            c = sscanner.Get();
          } // while

        } // if
        else {
          sscanner.LastTokens();
          throw new Lexical_error( '/', MyScanner.sline );
        } // else

      } // if

    } // else

    if ( MyScanner.stokenBuffer.size() > 0 ) {
      MyToken temp = MyScanner.stokenBuffer.remove( MyScanner.stokenBuffer.size() - 1 );
      temp.mline = 1;
      MyScanner.stokenBuffer.add( temp );
    } // if 
    
  // System.out.println("---------end User_input--------");

  } // User_input()

  boolean Definition() throws Exception {
    // System.out.println("---------Definition--------");

    MyToken token = new MyToken();
    token = sscanner.PeekToken();

    if ( token.mtype.equals( "VOID" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      svartype = token.mtype;
      token = sscanner.PeekToken();

      if ( token.mtype.equals( "IDENTIFIER" ) ) {
        token = sscanner.GetToken();
        token.mrtype = svartype;
        
        if ( stempvar.size() < 1 ) {
          stempvar.add( new ArrayList<MyToken>() );
        } // if
        
        stempvar.get( stempvar.size() - 1 ).add( new MyToken( token ) );
        scorrtoken.add( new MyToken( token ) );
        
        Function_definition_without_id();      
        
      } // if
      else {
        sscanner.LastTokens();
        throw new Syntax_error( token );
      } // else 

    } // if
    else if ( token.mtype.equals( "INT" ) || token.mtype.equals( "CHAR" ) ||
              token.mtype.equals( "FLOAT" ) || token.mtype.equals( "STRING" ) ||
              token.mtype.equals( "BOOL" ) ) {
      Type_specifier();
      token = sscanner.PeekToken();

      if ( token.mtype.equals( "IDENTIFIER" ) ) {
        token = sscanner.GetToken();
        token.mrtype = svartype;
        if ( stempvar.size() < 1 ) {
          stempvar.add( new ArrayList<MyToken>() );
        } // if
        
        stempvar.get( stempvar.size() - 1 ).add( new MyToken( token ) );
        scorrtoken.add( new MyToken( token ) );

        Function_definition_or_declarators();

      } // if
      else {
        sscanner.LastTokens();
        throw new Syntax_error( token );
      } // else 

    } // else if 
    else {
      sscanner.LastTokens();
      throw new Syntax_error( token );
    } // else


    // System.out.println("---------end Definition--------");

    return true;
  } // Definition()

  boolean Type_specifier() throws Exception {
    // System.out.println("---------Type_specifier--------");

    MyToken token = new MyToken();
    token = sscanner.PeekToken();

    if ( token.mtype.equals( "INT" ) || token.mtype.equals( "CHAR" ) ||
         token.mtype.equals( "FLOAT" ) || token.mtype.equals( "STRING" ) ||
         token.mtype.equals( "BOOL" ) ) {
      token = sscanner.GetToken();
      svartype = token.mtype;
      scorrtoken.add( new MyToken( token ) );
    } // if
    else {
      sscanner.LastTokens();
      throw new Syntax_error( token );
    } // else 


    // System.out.println("---------end Type_specifier--------");

    return true;
  } // Type_specifier()

  boolean Function_definition_or_declarators() throws Exception {
    // System.out.println("---------Function_definition_or_declarators--------");

    MyToken token = new MyToken();
    token = sscanner.PeekToken();

    if ( token.mtype.equals( "(" ) ) {
      
      Function_definition_without_id();  

    } // if 
    else if ( token.mtype.equals( "[" ) || token.mtype.equals( "," ) ||
              token.mtype.equals( ";" ) ) { 
      Rest_of_declarators();
    } // else if
    else {
      sscanner.LastTokens();
      throw new Syntax_error( token );
    } // else 

    //  System.out.println("---------end Function_definition_or_declarators--------");

    return true;
  } // Function_definition_or_declarators()

  boolean Rest_of_declarators() throws Exception {
    //  System.out.println("---------Rest_of_declarators--------");

    MyToken token = new MyToken();
    token = sscanner.PeekToken();

    if ( token.mtype.equals( "[" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      token = sscanner.PeekToken();

      if ( token.mtype.equals( "CONSTANT" ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );
        token = sscanner.PeekToken();

        if ( token.mtype.equals( "]" ) ) {
          token = sscanner.GetToken();
          scorrtoken.add( new MyToken( token ) );
          token = sscanner.PeekToken();
        } // if 
        else {
          sscanner.LastTokens();
          throw new Syntax_error( token );
        } // else 

      } // if 
      else {
        sscanner.LastTokens();
        throw new Syntax_error( token );
      } // else 

    } // if 

    while ( token.mtype.equals( "," ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      token = sscanner.PeekToken();
      
      if ( token.mtype.equals( "IDENTIFIER" ) ) {
        token = sscanner.GetToken();
        token.mrtype = svartype;
        stempvar.get( stempvar.size() - 1 ).add( new MyToken( token ) );
        scorrtoken.add( new MyToken( token ) );
        token = sscanner.PeekToken();

        if ( token.mtype.equals( "[" ) ) {
          token = sscanner.GetToken();
          scorrtoken.add( new MyToken( token ) );
          token = sscanner.PeekToken();

          if ( token.mtype.equals( "CONSTANT" ) ) {
            token = sscanner.GetToken();
            scorrtoken.add( new MyToken( token ) );
            token = sscanner.PeekToken();

            if ( token.mtype.equals( "]" ) ) {
              token = sscanner.GetToken();
              scorrtoken.add( new MyToken( token ) );
              // token = sscanner.PeekToken();
            } // if 
            else {
              sscanner.LastTokens();
              throw new Syntax_error( token );
            } // else 

          } // if 
          else {
            sscanner.LastTokens();
            throw new Syntax_error( token ); 
          } // else 
        } // if 

      } // if
      else {
        sscanner.LastTokens();
        throw new Syntax_error( token );
      } // else

      token = sscanner.PeekToken();
    } // while

    if ( token.mtype.equals( ";" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );

    } // if 
    else {
      sscanner.LastTokens();
      throw new Syntax_error( token );
    } // else 

    // System.out.println("---------end Rest_of_declarators--------");

    return true;
  } // Rest_of_declarators()

  boolean Function_definition_without_id() throws Exception {
    // System.out.println("---------Function_definition_without_id--------");

    MyToken token = new MyToken();
    token = sscanner.PeekToken();

    if ( token.mtype.equals( "(" ) ) {
      MyToken token2 = scorrtoken.get( scorrtoken.size() - 1 );
      token2.mfunc = true;
      
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      token = sscanner.PeekToken();

      if ( token.mtype.equals( "VOID" ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );
        token = sscanner.PeekToken();
      } // if
      else if ( token.mtype.equals( "INT" ) || token.mtype.equals( "CHAR" ) ||
                token.mtype.equals( "FLOAT" ) || token.mtype.equals( "STRING" ) ||
                token.mtype.equals( "BOOL" ) ) {
        Formal_parameter_list();
        token = sscanner.PeekToken();
      } // else if 

      if ( token.mtype.equals( ")" ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );

        Compound_statement();
      } // if
      else {
        sscanner.LastTokens();
        throw new Syntax_error( token );
      } // else

    } // if 
    else {
      sscanner.LastTokens();
      throw new Syntax_error( token );
    } // else

    // System.out.println("---------end Function_definition_without_id--------");

    return true;
  } // Function_definition_without_id()     


  boolean Formal_parameter_list() throws Exception {
    // System.out.println("---------Formal_parameter_list--------");

    MyToken token = new MyToken();
    token = sscanner.PeekToken();

    Type_specifier();

    token = sscanner.PeekToken();
    if ( token.mtype.equals( "&" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      token = sscanner.PeekToken();
    } // if 

    if ( token.mtype.equals( "IDENTIFIER" ) ) {
      token = sscanner.GetToken();
      token.mrtype = svartype;
      stempvar.get( stempvar.size() - 1 ).add( new MyToken( token ) );
      scorrtoken.add( new MyToken( token ) );
      token = sscanner.PeekToken();
      

      if ( token.mtype.equals( "[" ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );
        token = sscanner.PeekToken();

        if ( token.mtype.equals( "CONSTANT" ) ) {
          token = sscanner.GetToken();
          scorrtoken.add( new MyToken( token ) );
          token = sscanner.PeekToken();

          if ( token.mtype.equals( "]" ) ) {
            token = sscanner.GetToken();
            scorrtoken.add( new MyToken( token ) );
            token = sscanner.PeekToken();
          } // if 
          else {
            sscanner.LastTokens();
            throw new Syntax_error( token );
          } // else

        } // if
        else {
          sscanner.LastTokens();
          throw new Syntax_error( token );
        } // else

      } // if

      while ( token.mtype.equals( "," ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );
        token = sscanner.PeekToken();

        Type_specifier();

        token = sscanner.PeekToken();
        if ( token.mtype.equals( "&" ) ) {
          token = sscanner.GetToken();
          scorrtoken.add( new MyToken( token ) );
          token = sscanner.PeekToken();
        } // if 

        if ( token.mtype.equals( "IDENTIFIER" ) ) {
          token = sscanner.GetToken();
          token.mrtype = svartype;
          stempvar.get( stempvar.size() - 1 ).add( new MyToken( token ) );
          scorrtoken.add( new MyToken( token ) );
          token = sscanner.PeekToken();
          
          if ( token.mtype.equals( "[" ) ) {
            token = sscanner.GetToken();
            scorrtoken.add( new MyToken( token ) );
            token = sscanner.PeekToken();
  
            if ( token.mtype.equals( "CONSTANT" ) ) {
              token = sscanner.GetToken();
              scorrtoken.add( new MyToken( token ) );
              token = sscanner.PeekToken();
  
              if ( token.mtype.equals( "]" ) ) {
                token = sscanner.GetToken();
                scorrtoken.add( new MyToken( token ) );
                // token = sscanner.PeekToken();
              } // if
              else {
                sscanner.LastTokens();
                throw new Syntax_error( token );
              } // else

            } // if
            else {
              sscanner.LastTokens();
              throw new Syntax_error( token );
            } // else

          } // if
        
        } // if
        else {
          sscanner.LastTokens();
          throw new Syntax_error( token );
        } // else 

        token = sscanner.PeekToken();
      } // while 

    } // if
    else {
      sscanner.LastTokens();
      throw new Syntax_error( token );
    } // else

    // System.out.println("---------end Formal_parameter_list--------");

    return true;
  } // Formal_parameter_list()

  boolean Compound_statement() throws Exception {
    // System.out.println("---------Compound_statement--------");

    MyToken token = new MyToken();
    token = sscanner.PeekToken();

    if ( token.mtype.equals( "{" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      stempvar.add( new ArrayList<MyToken>() );
      token = sscanner.PeekToken();
      

      while ( token.mtype.equals( "CHAR" ) || token.mtype.equals( "INT" ) ||
              token.mtype.equals( "FLOAT" ) || token.mtype.equals( "STRING" ) ||
              token.mtype.equals( "BOOL" ) || token.mtype.equals( ";" ) || 
              token.mtype.equals( "RETURN" ) || token.mtype.equals( "IF" ) || 
              token.mtype.equals( "WHILE" ) || token.mtype.equals( "DO" ) || 
              token.mtype.equals( "{" ) || token.mtype.equals( "IDENTIFIER" ) || 
              token.mtype.equals( "PP" ) || token.mtype.equals( "MM" ) ||
              token.mtype.equals( "-" ) || token.mtype.equals( "+" ) ||
              token.mtype.equals( "!" ) || token.mtype.equals( "CONSTANT" ) ||
              token.mtype.equals( "(" ) ) {

        if ( token.mtype.equals( "CHAR" ) || token.mtype.equals( "INT" ) ||
             token.mtype.equals( "FLOAT" ) || token.mtype.equals( "STRING" ) ||
             token.mtype.equals( "BOOL" ) ) {
          Declaration();
        } // if 
        else {
          Statement();
        } // else 

        token = sscanner.PeekToken();
      } // while

      if ( token.mtype.equals( "}" ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );
        stempvar.remove( stempvar.size() - 1 );
      } // if 
      else {
        sscanner.LastTokens();
        throw new Syntax_error( token );
      } // else
    
    } // if 
    else {
      sscanner.LastTokens();
      throw new Syntax_error( token );
    } // else

    //      System.out.println("---------end Compound_statement--------");
    return true;
  } // Compound_statement()


  boolean Declaration() throws Exception {
    // System.out.println("---------Declaration--------");

    MyToken token = new MyToken();
    token = sscanner.PeekToken();

    Type_specifier();

    token = sscanner.PeekToken();
    if ( token.mtype.equals( "IDENTIFIER" ) ) {
      token = sscanner.GetToken();
      token.mrtype = svartype;
      stempvar.get( stempvar.size() - 1 ).add( new MyToken( token ) );
      scorrtoken.add( new MyToken( token ) );
      
      Rest_of_declarators();
    } // if
    else {
      sscanner.LastTokens();
      throw new Syntax_error( token ); 
    } // else 
        
    // System.out.println("--------- end Declaration--------");

    return true;
  } // Declaration()


  void Statement() throws Exception { 
    // System.out.println("---------Statement--------");

    MyToken token = new MyToken();
    token = sscanner.PeekToken();

    if ( token.mtype.equals( ";" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
    } // if 
    else if ( token.mtype.equals( "IDENTIFIER" ) || token.mtype.equals( "PP" ) || 
              token.mtype.equals( "MM" ) || token.mtype.equals( "-" ) ||
              token.mtype.equals( "+" ) || token.mtype.equals( "!" ) ||
              token.mtype.equals( "CONSTANT" ) || token.mtype.equals( "(" ) ) {
      Expression();
      token = sscanner.PeekToken();
      if ( token.mtype.equals( ";" ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );
        
      }  // if 
      else {
        sscanner.LastTokens();
        throw new Syntax_error( token );
      } // else 

    } // else if 
    else if ( token.mtype.equals( "RETURN" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      token = sscanner.PeekToken();

      if ( token.mtype.equals( "IDENTIFIER" ) || token.mtype.equals( "PP" ) || 
           token.mtype.equals( "MM" ) || token.mtype.equals( "-" ) ||
           token.mtype.equals( "+" ) || token.mtype.equals( "!" ) ||
           token.mtype.equals( "CONSTANT" ) || token.mtype.equals( "(" ) ) {
        Expression();
        token = sscanner.PeekToken();
      } // if

      if ( token.mtype.equals( ";" ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );
      } // if

    } // else if 
    else if ( token.mtype.equals( "{" ) ) {
      Compound_statement();
    } // else if
    else if ( token.mtype.equals( "IF" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      token = sscanner.PeekToken();

      if ( token.mtype.equals( "(" ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );

        Expression();
        token = sscanner.PeekToken();

        if ( token.mtype.equals( ")" ) ) {
          token = sscanner.GetToken();
          scorrtoken.add( new MyToken( token ) );

          Statement();
          token = sscanner.PeekToken();
          
          if ( token.mtype.equals( "ELSE" ) ) {
            token = sscanner.GetToken();
            scorrtoken.add( new MyToken( token ) );

            Statement();
          }  // if  

        } // if
        else {
          sscanner.LastTokens();
          throw new Syntax_error( token );
        } // else

      } // if
      else {
        sscanner.LastTokens();
        throw new Syntax_error( token );
      } // else

    } // else if
    else if ( token.mtype.equals( "WHILE" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      token = sscanner.PeekToken();

      if ( token.mtype.equals( "(" ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );
        Expression();

        token = sscanner.PeekToken();
        if ( token.mtype.equals( ")" ) ) {
          token = sscanner.GetToken();
          scorrtoken.add( new MyToken( token ) );

          Statement();
        } // if
        else {
          sscanner.LastTokens();
          throw new Syntax_error( token );
        } // else
        
      } // if
      else {
        sscanner.LastTokens();
        throw new Syntax_error( token );
      } // else 

    } // else if
    else if ( token.mtype.equals( "DO" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );

      Statement();
      token = sscanner.PeekToken();

      if ( token.mtype.equals( "WHILE" ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );
        token = sscanner.PeekToken();

        if ( token.mtype.equals( "(" ) ) {
          token = sscanner.GetToken();
          scorrtoken.add( new MyToken( token ) );

          Expression();
          token = sscanner.PeekToken();

          if ( token.mtype.equals( ")" ) ) {
            token = sscanner.GetToken();
            scorrtoken.add( new MyToken( token ) );
            token = sscanner.PeekToken();

            if ( token.mtype.equals( ";" ) ) {
              token = sscanner.GetToken();
              scorrtoken.add( new MyToken( token ) );
            } // if
            else {
              sscanner.LastTokens();
              throw new Syntax_error( token );
            } // else

          } // if
          else {
            sscanner.LastTokens();
            throw new Syntax_error( token );
          } // else

        } // if
        else {
          sscanner.LastTokens();
          throw new Syntax_error( token );
        } // else 

      } // if
      else {
        sscanner.LastTokens();
        throw new Syntax_error( token );
      } // else

    } // else if
    else {
      sscanner.LastTokens();
      throw new Syntax_error( token );
    } // else

    // System.out.println("---------end Statement--------");

  } // Statement()

  boolean Expression() throws Exception {
    // System.out.println("---------Expression--------");

    MyToken token = new MyToken();
    Basic_expression();

    token = sscanner.PeekToken();
    while ( token.mtype.equals( "," ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      
      Basic_expression();

      token = sscanner.PeekToken();
    } // while 

    // System.out.println("---------end Expression--------");

    return true;
  } // Expression()

  boolean Basic_expression() throws Exception {
    //  System.out.println("---------Basic_expression--------");

    MyToken token = new MyToken();
    token = sscanner.PeekToken();
    
    if ( token.mtype.equals( "IDENTIFIER" ) ) {
      if ( !Isdefined( token ) ) {
        sscanner.LastTokens();
        throw new Semantic_error( token );
      } // if
      
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );

      Rest_of_identifier_started_basic_exp();
    } // if
    else if ( token.mtype.equals( "PP" ) || token.mtype.equals( "MM" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );

      token = sscanner.PeekToken();
      if ( token.mtype.equals( "IDENTIFIER" ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );

        Rest_of_ppmm_identifier_started_basic_exp();
      } // if
      else {
        sscanner.LastTokens();
        throw new Syntax_error( token );
      } // else

    } // else if 
    else if ( token.mtype.equals( "+" ) || token.mtype.equals( "-" ) ||
              token.mtype.equals( "!" ) ) {
      Sign();
      token = sscanner.PeekToken();
      while ( token.mtype.equals( "+" ) || token.mtype.equals( "-" ) ||
              token.mtype.equals( "!" ) ) {
        Sign();
        token = sscanner.PeekToken();
      } // while 

      Signed_unary_exp();

      Romce_and_romloe();

    } // else if
    else if ( token.mtype.equals( "CONSTANT" ) || token.mtype.equals( "(" ) ) {
      if ( token.mtype.equals( "CONSTANT" ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( token );
      } // if
      else if ( token.mtype.equals( "(" ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );

        Expression();
        token = sscanner.PeekToken();
        if ( token.mtype.equals( ")" ) ) {
          token = sscanner.GetToken();
          scorrtoken.add( new MyToken( token ) );
        } // if

      } // else if

      Romce_and_romloe();

    } // else if
    else {
      sscanner.LastTokens();
      throw new Syntax_error( token );
    } // else 

    // System.out.println("---------end Basic_expression--------");

    return true;
  } // Basic_expression()

  boolean Rest_of_identifier_started_basic_exp() throws Exception {
    // System.out.println("---------Rest_of_identifier_started_basic_exp--------");

    MyToken token = new MyToken();
    token = sscanner.PeekToken();

    if ( token.mtype.equals( "[" ) || token.mtype.equals( "=" ) ||
         token.mtype.equals( "TE" ) || token.mtype.equals( "DE" ) ||
         token.mtype.equals( "RE" ) || token.mtype.equals( "PE" ) || 
         token.mtype.equals( "ME" ) || token.mtype.equals( "PP" ) ||
         token.mtype.equals( "MM" ) || token.mtype.equals( "*" ) || 
         token.mtype.equals( "/" ) || token.mtype.equals( "%" ) ||
         token.mtype.equals( "+" ) || token.mtype.equals( "-" ) ||
         token.mtype.equals( "LS" ) || token.mtype.equals( "RS" ) ||
         token.mtype.equals( "<" ) || token.mtype.equals( ">" ) || 
         token.mtype.equals( "LE" ) || token.mtype.equals( "GE" ) ||
         token.mtype.equals( "EQ" ) || token.mtype.equals( "NEQ" ) ||
         token.mtype.equals( "&" ) || token.mtype.equals( "^" ) ||
         token.mtype.equals( "|" ) || token.mtype.equals( "AND" ) ||
         token.mtype.equals( "OR" ) || token.mtype.equals( "?" ) ) {
      
      if ( token.mtype.equals( "[" ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );

        Expression();
        token = sscanner.PeekToken();
        if ( token.mtype.equals( "]" ) ) {
          token = sscanner.GetToken();
          scorrtoken.add( new MyToken( token ) );
          token = sscanner.PeekToken();
        } // if
        else {
          sscanner.LastTokens();
          throw new Syntax_error( token );
        } // else

      } // if
      
      if ( token.mtype.equals( "=" ) || token.mtype.equals( "TE" ) ||
           token.mtype.equals( "DE" ) || token.mtype.equals( "RE" ) ||
           token.mtype.equals( "PE" ) || token.mtype.equals( "ME" ) ) {
        
        Assignment_operator();
        Basic_expression();
      } // if
      else {
        if ( token.mtype.equals( "PP" ) || token.mtype.equals( "MM" ) ) {
          token = sscanner.GetToken();
          scorrtoken.add( new MyToken( token ) );
        } // if

        Romce_and_romloe();
      } // else 

    } // if
    else if ( token.mtype.equals( "(" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      token = sscanner.PeekToken();

      if ( token.mtype.equals( "IDENTIFIER" ) || token.mtype.equals( "PP" ) || 
           token.mtype.equals( "MM" ) || token.mtype.equals( "-" ) ||
           token.mtype.equals( "+" ) || token.mtype.equals( "!" ) ||
           token.mtype.equals( "CONSTANT" ) || token.mtype.equals( "(" ) ) {
        Actual_parameter_list();
        token = sscanner.PeekToken();
      } // if

      if ( token.mtype.equals( ")" ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );
      } // if
      else {
        sscanner.LastTokens();
        throw new Syntax_error( token );
      } // else

      Romce_and_romloe();

    } // else if 

    // System.out.println("---------end Rest_of_identifier_started_basic_exp--------");

    return true;
  } // Rest_of_identifier_started_basic_exp()

  boolean Rest_of_ppmm_identifier_started_basic_exp() throws Exception {
    // System.out.println("---------Rest_of_ppmm_identifier_started_basic_exp--------");

    MyToken token = new MyToken();
    token = sscanner.PeekToken();

    if ( token.mtype.equals( "[" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );

      Expression();

      token = sscanner.PeekToken();
      if ( token.mtype.equals( "]" ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );
      } // if
      else {
        sscanner.LastTokens();
        throw new Syntax_error( token );
      } // else 

    } // if

    Romce_and_romloe();

    // System.out.println("---------end Rest_of_ppmm_identifier_started_basic_exp--------");

    return true;
  } // Rest_of_ppmm_identifier_started_basic_exp()

  boolean Sign() throws Exception {
    // System.out.println("---------Sign--------");

    MyToken token = new MyToken();
    token = sscanner.PeekToken();

    if ( token.mtype.equals( "+" ) || token.mtype.equals( "-" ) ||
         token.mtype.equals( "!" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
    } // if
    else {
      sscanner.LastTokens();
      throw new Syntax_error( token );
    } // else

    // System.out.println("---------end Sign--------");

    return true;
  } // Sign()

  boolean Actual_parameter_list() throws Exception {
    // System.out.println("---------Actual_parameter_list--------");
    MyToken token = new MyToken();
    Basic_expression();

    token = sscanner.PeekToken();
    while ( token.mtype.equals( "," ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      
      Basic_expression();

      token = sscanner.PeekToken();
    } // while 

    // System.out.println("--------end Actual_parameter_list-------------");
    return true;
  } // Actual_parameter_list()

  boolean Assignment_operator() throws Exception {
    // System.out.println("---------Assignment_operator--------");
    MyToken token = new MyToken();
    token = sscanner.PeekToken();
    if ( token.mtype.equals( "=" ) || token.mtype.equals( "TE" ) ||
         token.mtype.equals( "DE" ) || token.mtype.equals( "RE" ) ||
         token.mtype.equals( "PE" ) || token.mtype.equals( "ME" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
    } // if
    else {
      sscanner.LastTokens();
      throw new Syntax_error( token );
    } // else 

    
    // System.out.println("---------end Assignment_operator--------");
    return true;
  } // Assignment_operator()

  boolean Romce_and_romloe() throws Exception {
    // System.out.println("---------Romce_and_romloe--------");
    MyToken token = new MyToken();
    Rest_of_maybe_logical_or_exp();
    token = sscanner.PeekToken();

    if ( token.mtype.equals( "?" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      
      Basic_expression();
      token = sscanner.PeekToken();
      // System.out.println("1366: name: " + token.mname + " type: " + token.mtype);
      if ( token.mtype.equals( ":" ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );
        Basic_expression();
      } // if
      else {
        sscanner.LastTokens();
        throw new Syntax_error( token );
      } // else 

    } // if

    // System.out.println("---------end Romce_and_romloe--------");
    return true;
  } // Romce_and_romloe()

  boolean Rest_of_maybe_logical_or_exp() throws Exception {
    // System.out.println("---------Rest_of_maybe_logical_or_exp--------");

    MyToken token = new MyToken();
    Rest_of_maybe_logical_and_exp();
    token = sscanner.PeekToken();

    while ( token.mtype.equals( "OR" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      Maybe_logical_and_exp();
      token = sscanner.PeekToken();
    } // while 

    // System.out.println("---------end Rest_of_maybe_logical_or_exp--------");
    return true;
  } // Rest_of_maybe_logical_or_exp()

  boolean Maybe_logical_and_exp() throws Exception {
    // System.out.println("---------Maybe_logical_and_exp--------");

    MyToken token = new MyToken();
    Maybe_bit_or_exp();
    token = sscanner.PeekToken();

    while ( token.mtype.equals( "AND" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      Maybe_bit_or_exp();
      token = sscanner.PeekToken();
    } // while 

    // System.out.println("---------end Maybe_logical_and_exp--------");

    return true;
  } // Maybe_logical_and_exp()

  boolean Rest_of_maybe_logical_and_exp() throws Exception {
    // System.out.println("---------Rest_of_maybe_logical_and_exp--------");
    MyToken token = new MyToken();
    Rest_of_maybe_bit_or_exp();
    token = sscanner.PeekToken();

    while ( token.mtype.equals( "AND" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      Maybe_bit_or_exp();
      token = sscanner.PeekToken();
    } // while 

    // System.out.println("---------end Rest_of_maybe_logical_and_exp--------");

    return true;
  } // Rest_of_maybe_logical_and_exp()

  boolean Maybe_bit_or_exp() throws Exception {
    // System.out.println("---------Maybe_bit_or_exp--------");

    MyToken token = new MyToken();
    Maybe_bit_ex_or_exp();
    token = sscanner.PeekToken();

    while ( token.mtype.equals( "|" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      Maybe_bit_ex_or_exp();
      token = sscanner.PeekToken();
    } // while 

    // System.out.println("---------end Maybe_bit_or_exp--------");

    return true;
  } // Maybe_bit_or_exp()

  boolean Rest_of_maybe_bit_or_exp() throws Exception {
    // System.out.println("---------Rest_of_maybe_bit_or_exp--------");

    MyToken token = new MyToken();
    Rest_of_maybe_bit_ex_or_exp();
    token = sscanner.PeekToken();

    while ( token.mtype.equals( "|" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      Maybe_bit_ex_or_exp();
      token = sscanner.PeekToken();
    } // while 
    
    // System.out.println("---------end Rest_of_maybe_bit_or_exp--------");
    return true;
  } // Rest_of_maybe_bit_or_exp()

  boolean Maybe_bit_ex_or_exp() throws Exception {
    // System.out.println("---------Rest_of_maybe_bit_ex_or_exp--------");
    MyToken token = new MyToken();
    Maybe_bit_and_exp();
    token = sscanner.PeekToken();

    while ( token.mtype.equals( "^" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      Maybe_bit_and_exp();
      token = sscanner.PeekToken();
    } // while 

    // System.out.println("---------end Rest_of_maybe_bit_ex_or_exp--------");

    return true;
  } // Maybe_bit_ex_or_exp()

  boolean Rest_of_maybe_bit_ex_or_exp() throws Exception {
    // System.out.println("---------Rest_of_maybe_bit_ex_or_exp--------");

    MyToken token = new MyToken();
    Rest_of_maybe_bit_and_exp();
    token = sscanner.PeekToken();

    while ( token.mtype.equals( "^" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      Maybe_bit_and_exp();
      token = sscanner.PeekToken();
    } // while 
    
    // System.out.println("---------end Rest_of_maybe_bit_ex_or_exp--------");

    return true;
  } // Rest_of_maybe_bit_ex_or_exp()

  boolean Maybe_bit_and_exp() throws Exception {
    // System.out.println("---------Maybe_bit_and_exp--------");

    MyToken token = new MyToken();
    Maybe_equality_exp();
    token = sscanner.PeekToken();

    while ( token.mtype.equals( "&" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      Maybe_equality_exp();
      token = sscanner.PeekToken();
    } // while 

    // System.out.println("---------end Maybe_bit_and_exp--------");

    return true;
  } // Maybe_bit_and_exp()

  boolean Rest_of_maybe_bit_and_exp() throws Exception {
    // System.out.println("---------Rest_of_maybe_bit_and_exp--------");

    MyToken token = new MyToken();
    Rest_of_maybe_equality_exp();
    token = sscanner.PeekToken();

    while ( token.mtype.equals( "&" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      Maybe_equality_exp();
      token = sscanner.PeekToken();
    } // while 
    
    // System.out.println("---------end Rest_of_maybe_bit_and_exp--------");

    return true;
  } // Rest_of_maybe_bit_and_exp()

  boolean Maybe_equality_exp() throws Exception {
    // System.out.println("---------Maybe_equality_exp--------");

    MyToken token = new MyToken();
    Maybe_relational_exp();
    token = sscanner.PeekToken();

    while ( token.mtype.equals( "EQ" ) || token.mtype.equals( "NEQ" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      Maybe_relational_exp();
      token = sscanner.PeekToken();
    } // while
    
    // System.out.println("---------end Maybe_equality_exp--------");
    return true;
  } // Maybe_equality_exp()

  boolean Rest_of_maybe_equality_exp() throws Exception {
    // System.out.println("---------Rest_of_maybe_equality_exp--------");

    MyToken token = new MyToken();
    Rest_of_maybe_relational_exp();
    token = sscanner.PeekToken();

    while ( token.mtype.equals( "EQ" ) || token.mtype.equals( "NEQ" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      Maybe_relational_exp();
      token = sscanner.PeekToken();
    } // while

    // System.out.println("---------end Rest_of_maybe_equality_exp--------");

    return true;
  } // Rest_of_maybe_equality_exp()

  boolean Maybe_relational_exp() throws Exception {
    // System.out.println("---------Maybe_relational_exp--------");

    MyToken token = new MyToken();
    Maybe_shift_exp();

    token = sscanner.PeekToken();
    while ( token.mtype.equals( "<" ) || token.mtype.equals( ">" ) ||
            token.mtype.equals( "LE" ) || token.mtype.equals( "GE" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      Maybe_shift_exp();
      token = sscanner.PeekToken();
    } // while
    
    // System.out.println("---------end Maybe_relational_exp--------");

    return true;
  } // Maybe_relational_exp()

  boolean Rest_of_maybe_relational_exp() throws Exception {
    // System.out.println("---------Rest_of_maybe_relational_exp--------");

    MyToken token = new MyToken();
    Rest_of_maybe_shift_exp();

    token = sscanner.PeekToken();
    while ( token.mtype.equals( "<" ) || token.mtype.equals( ">" ) ||
            token.mtype.equals( "LE" ) || token.mtype.equals( "GE" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      Maybe_shift_exp();
      token = sscanner.PeekToken();
    } // while
    
    // System.out.println("---------end Rest_of_maybe_relational_exp--------");

    return true;
  } // Rest_of_maybe_relational_exp()

  boolean Maybe_shift_exp() throws Exception {
    // System.out.println("---------Maybe_shift_exp--------");

    MyToken token = new MyToken();
    Maybe_additive_exp();
    token = sscanner.PeekToken();

    while ( token.mtype.equals( "LS" ) || token.mtype.equals( "RS" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );

      Maybe_additive_exp();
      token = sscanner.PeekToken();
    } // while 

    // System.out.println("---------end Maybe_shift_exp--------");

    return true;
  } // Maybe_shift_exp()

  boolean Rest_of_maybe_shift_exp() throws Exception {
    // System.out.println("---------Rest_of_maybe_shift_exp--------");

    MyToken token = new MyToken();
    Rest_of_maybe_additive_exp();
    token = sscanner.PeekToken();

    while ( token.mtype.equals( "LS" ) || token.mtype.equals( "RS" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );

      Maybe_additive_exp();
      token = sscanner.PeekToken();
    } // while 


    // System.out.println("---------end Rest_of_maybe_shift_exp--------");
    return true;
  } // Rest_of_maybe_shift_exp()

  boolean Maybe_additive_exp() throws Exception {
    // System.out.println("---------Maybe_additive_exp--------");

    MyToken token = new MyToken();

    Maybe_mult_exp();
    token = sscanner.PeekToken();
    while ( token.mtype.equals( "+" ) || token.mtype.equals( "-" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );

      Maybe_mult_exp();
      token = sscanner.PeekToken();
    } // while

    // System.out.println("---------end Maybe_additive_exp--------");
    return true;
  } // Maybe_additive_exp()

  boolean Rest_of_maybe_additive_exp() throws Exception {
    // System.out.println("---------Rest_of_maybe_additive_exp--------");

    MyToken token = new MyToken();
    Rest_of_maybe_mult_exp();

    token = sscanner.PeekToken();

    while ( token.mtype.equals( "+" ) || token.mtype.equals( "-" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );

      Maybe_mult_exp();
      token = sscanner.PeekToken();
    } // while
    
    // System.out.println("---------end Rest_of_maybe_additive_exp--------");
    return true;
  } // Rest_of_maybe_additive_exp()



  boolean Maybe_mult_exp() throws Exception {
    // System.out.println("---------Maybe_mult_exp--------");

    Unary_exp();
    Rest_of_maybe_mult_exp();
    // System.out.println("---------end Maybe_mult_exp--------");
    return true;
  } // Maybe_mult_exp()

  boolean Rest_of_maybe_mult_exp() throws Exception {
    // System.out.println("---------Rest_of_maybe_mult_exp--------");
    MyToken token = new MyToken();
    token = sscanner.PeekToken();

    while ( token.mtype.equals( "*" )  || token.mtype.equals( "/" ) ||
            token.mtype.equals( "%" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );

      Unary_exp();
      token = sscanner.PeekToken();
    } // while
    
    // System.out.println("---------end Rest_of_maybe_mult_exp--------");
    return true;
  } // Rest_of_maybe_mult_exp()

  boolean Unary_exp() throws Exception {
    // System.out.println("---------Unary_exp--------");

    MyToken token = new MyToken();
    token = sscanner.PeekToken();

    if ( token.mtype.equals( "+" ) || token.mtype.equals( "-" ) || 
         token.mtype.equals( "!" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      token = sscanner.PeekToken();

      while ( token.mtype.equals( "+" ) || token.mtype.equals( "-" ) || 
              token.mtype.equals( "!" ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );
        token = sscanner.PeekToken();
      } // while
      
      Signed_unary_exp();

    } // if
    else if ( token.mtype.equals( "IDENTIFIER" ) || token.mtype.equals( "CONSTANT" ) ||
              token.mtype.equals( "(" ) ) {
      Unsigned_unary_exp();
    } // else if
    else if ( token.mtype.equals( "PP" ) || token.mtype.equals( "MM" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      token = sscanner.PeekToken();
      
      if ( token.mtype.equals( "IDENTIFIER" ) ) {
        if ( !Isdefined( token ) ) {
          sscanner.LastTokens();
          throw new Semantic_error( token );
        } // if
        
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );
        
        token = sscanner.PeekToken();
        if ( token.mtype.equals( "[" ) ) {
          token = sscanner.GetToken();
          scorrtoken.add( new MyToken( token ) );
          
          Expression();
          token = sscanner.PeekToken();
          if ( token.mtype.equals( "]" ) ) {
            token = sscanner.GetToken();
            scorrtoken.add( new MyToken( token ) );
          } // if
          else {        
            sscanner.LastTokens();
            throw new Syntax_error( token );            
          } // else
          
        } // if
        
      } // if
      else {
        sscanner.LastTokens();
        throw new Syntax_error( token );
      } // else
      
    } // else if 
    else {
      sscanner.LastTokens();
      throw new Syntax_error( token );
    } // else

    //  System.out.println("---------end Unary_exp--------");
    return true;
  } // Unary_exp()

  boolean Signed_unary_exp() throws Exception {
    // System.out.println("---------Signed_unary_exp--------");

    MyToken token = new MyToken();
    token = sscanner.PeekToken();

    if ( token.mtype.equals( "IDENTIFIER" ) ) {
      if ( !Isdefined( token ) ) {
        sscanner.LastTokens();
        throw new Semantic_error( token );
      } // if
      
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      token = sscanner.PeekToken();

      if ( token.mtype.equals( "(" ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );
        token = sscanner.PeekToken();

        if ( token.mtype.equals( "IDENTIFIER" ) || token.mtype.equals( "PP" ) || 
             token.mtype.equals( "MM" ) || token.mtype.equals( "-" ) ||
             token.mtype.equals( "+" ) || token.mtype.equals( "!" ) ||
             token.mtype.equals( "CONSTANT" ) || token.mtype.equals( "(" ) ) {
          Actual_parameter_list();
          token = sscanner.PeekToken();
        } // if

        if ( token.mtype.equals( ")" ) ) {
          token = sscanner.GetToken();
          scorrtoken.add( new MyToken( token ) );
        } // if
        else {
          sscanner.LastTokens();
          throw new Syntax_error( token );
        } // else

      } // if
      else if ( token.mtype.equals( "[" ) ) {
        if ( token.mtype.equals( "[" ) ) {
          token = sscanner.GetToken();
          scorrtoken.add( new MyToken( token ) );
          Expression();
          token = sscanner.PeekToken();
          if ( token.mtype.equals( "]" ) ) {
            token = sscanner.GetToken();
            scorrtoken.add( new MyToken( token ) );
            token = sscanner.PeekToken();
          } // if
          else {
            sscanner.LastTokens();
            throw new Syntax_error( token );
          } // else

        } // if

      } // else if

    } // if
    else if ( token.mtype.equals( "CONSTANT" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
    } // else if
    else if ( token.mtype.equals( "(" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );

      Expression();
      token = sscanner.PeekToken();
      if ( token.mtype.equals( ")" ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );
      } // if
      else {  
        sscanner.LastTokens();
        throw new Syntax_error( token );
      } // else

    } // else if
    else {
      sscanner.LastTokens();
      throw new Syntax_error( token );
    } // else

    // System.out.println("---------end Signed_unary_exp--------");
    return true;
  } // Signed_unary_exp()

  boolean Unsigned_unary_exp() throws Exception {
    // System.out.println("---------Unsigned_unary_exp--------");

    MyToken token = new MyToken();
    token = sscanner.PeekToken();

    if ( token.mtype.equals( "IDENTIFIER" ) ) {
      if ( !Isdefined( token ) ) {
        sscanner.LastTokens();
        throw new Semantic_error( token );
      } // if
      
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
      token = sscanner.PeekToken();

      if ( token.mtype.equals( "(" ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );
        token = sscanner.PeekToken();

        if ( token.mtype.equals( "IDENTIFIER" ) || token.mtype.equals( "PP" ) || 
             token.mtype.equals( "MM" ) || token.mtype.equals( "-" ) ||
             token.mtype.equals( "+" ) || token.mtype.equals( "!" ) ||
             token.mtype.equals( "CONSTANT" ) || token.mtype.equals( "(" ) ) {
          Actual_parameter_list();
          token = sscanner.PeekToken();
        } // if

        if ( token.mtype.equals( ")" ) ) {
          token = sscanner.GetToken();
          scorrtoken.add( new MyToken( token ) );
        } // if
        else {
          sscanner.LastTokens();
          throw new Syntax_error( token );
        } // else

      } // if
      else if ( token.mtype.equals( "[" ) || token.mtype.equals( "PP" ) || token.mtype.equals( "MM" ) ) {
        if ( token.mtype.equals( "[" ) ) {
          token = sscanner.GetToken();
          scorrtoken.add( new MyToken( token ) );
          Expression();
          token = sscanner.PeekToken();
          if ( token.mtype.equals( "]" ) ) {
            token = sscanner.GetToken();
            scorrtoken.add( new MyToken( token ) );
            token = sscanner.PeekToken();
          } // if
          else {
            sscanner.LastTokens();
            throw new Syntax_error( token );
          } // else

        } // if

        if ( token.mtype.equals( "PP" ) || token.mtype.equals( "MM" ) ) {
          token = sscanner.GetToken();
          scorrtoken.add( new MyToken( token ) );
        } // if

      } // else if

    } // if
    else if ( token.mtype.equals( "CONSTANT" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );
    } // else if
    else if ( token.mtype.equals( "(" ) ) {
      token = sscanner.GetToken();
      scorrtoken.add( new MyToken( token ) );

      Expression();
      token = sscanner.PeekToken();
      if ( token.mtype.equals( ")" ) ) {
        token = sscanner.GetToken();
        scorrtoken.add( new MyToken( token ) );
      } // if
      else {  
        sscanner.LastTokens();
        throw new Syntax_error( token );
      } // else

    } // else if
    else {
      sscanner.LastTokens();
      throw new Syntax_error( token );
    } // else

    // System.out.println("---------end Unsigned_unary_exp--------");
    return true;
  } // Unsigned_unary_exp()

} // class MyParser

class MyExecute {
  static ArrayList<MyToken> stokenlist;
  static ArrayList<ArrayList<MyToken> > stable = new ArrayList<ArrayList<MyToken> >();
  static int sindex = 0;
  
  MyExecute() {
    MyToken cout = new MyToken();
    cout.mname = new String( "cout" );
    stable.add( new ArrayList<MyToken>() );
    stable.get( stable.size() - 1 ).add( cout );
    
  } // MyExecute()
  
  void GetTokenList( ArrayList<MyToken> corrtokenlist ) {
    stokenlist = corrtokenlist;
  } // GetTokenList()
  
  MyToken Find( MyToken token ) {
    for ( int i = stable.size() - 1; i >= 0 ; i-- ) {
      for ( int j = stable.get( i ).size() - 1 ; j >= 0 ; j-- ) {
        if ( token.mname.equals( stable.get( i ).get( j ).mname ) ) {
          // int a; 0   int a(){} 1
          if ( ( !token.mfunc && !stable.get( i ).get( j ).mfunc ) ) { // both are variable
            return stable.get( i ).get( j );
          } // if
          else if ( ( token.mfunc && stable.get( i ).get( j ).mfunc ) ) { // both are function
            return stable.get( i ).get( j );
          } // else if
          
        } // if 
        
      } // for
      
    } // for
     
    return null; // not found
  } // Find()
  
  MyToken GetToken() {
    MyToken token = stokenlist.get( sindex ); 
    if ( sindex < stokenlist.size() - 1 ) {
      sindex += 1;
    } // if
    
    return new MyToken( token );
  } // GetToken()
  
  MyToken PeekToken() {
    MyToken token = stokenlist.get( sindex );     
    return token;
  } // PeekToken()
  
  void User_input() throws Exception {
    sindex = 0;
    MyToken token = PeekToken();
    
    if ( token.mtype.equals( "VOID" ) || token.mtype.equals( "INT" ) ||
         token.mtype.equals( "CHAR" ) || token.mtype.equals( "FLOAT" ) ||
         token.mtype.equals( "STRING" ) || token.mtype.equals( "BOOL" ) ) {
      Definition();
    } // if
    else {
      System.out.print( "> " );
      
      if ( token.mname.equals( "Done" ) ) {
        throw new Done();
      } // if 
      
      Statement();
      System.out.println( "Statement executed ..." );
    } // else
    
  } // User_input()
  
  void Definition() {
    MyToken where = null;
    MyToken token = PeekToken();
    if ( token.mtype.equals( "VOID" ) ) {
      token = GetToken(); // VOID
      
      MyToken id = GetToken(); // ID
      
      if ( stable.size() < 1 ) {
        stable.add( new ArrayList<MyToken>() );
      } // if
      
      where = Find( id );
      if ( where != null ) { // already defined, now updates the stable
        stable.get( stable.size() - 1 ).remove( where );
        stable.get( stable.size() - 1 ).add( id );
        if ( !id.mfunc )
          System.out.println( "> New definition of " + id.mname + " entered ..." );
        else 
          System.out.println( "> New definition of " + id.mname + "() entered ..." );
      } // if
      else { // first time defined, now add  to stable
        stable.get( stable.size() - 1 ).add( new MyToken( id ) );
        if ( !id.mfunc )
          System.out.println( "> Definition of " + id.mname + " entered ..." );
        else 
          System.out.println( "> Definition of " + id.mname + "() entered ..." );
      } // else
      
      Function_definition_without_id();
      
    } // if 
    else {
      token = GetToken(); // Type_specifier
      MyToken id = GetToken(); // ID
      
      Function_definition_or_declarators( id );
    } // else
    
  } // Definition()
  
  void Function_definition_or_declarators( MyToken id ) {
    MyToken where = null;
    MyToken token = PeekToken();
    
    if ( token.mtype.equals( "(" ) ) {
      if ( stable.size() < 1 ) {
        stable.add( new ArrayList<MyToken>() );
      } // if
      
      where = Find( id );
      if ( where != null ) { // already defined, now updates the stable
        stable.get( stable.size() - 1 ).remove( where );
        stable.get( stable.size() - 1 ).add( id );
        if ( !id.mfunc )
          System.out.println( "> New definition of " + id.mname + " entered ..." );
        else 
          System.out.println( "> New definition of " + id.mname + "() entered ..." );
      } // if
      else { // first time defined, now add  to stable
        stable.get( stable.size() - 1 ).add( new MyToken( id ) );
        if ( !id.mfunc )
          System.out.println( "> Definition of " + id.mname + " entered ..." );
        else 
          System.out.println( "> Definition of " + id.mname + "() entered ..." );
      } // else
      
      Function_definition_without_id();
      
    } // if
    else {
      Rest_of_declarators( id, true );
    } // else
    
  } // Function_definition_or_declarators()
  
  void Addmlist( MyToken id ) {
    id.mlist = new ArrayList<String>();
    for ( int i = 0; i < id.mlength ; i++ ) {      
      id.mlist.add( "" );
    } // for

  } // Addmlist()
  
  void Rest_of_declarators( MyToken id, boolean print ) {
    MyToken token = PeekToken();
    MyToken where = null;
    id.mindex = 0;
    if ( token.mtype.equals( "[" ) ) {
      token = GetToken(); // [
      token = GetToken(); // CONSTANT
      id.mlength = Integer.parseInt( token.mname );
      token = GetToken(); // ]
      token = PeekToken();
    } // if
    
    Addmlist( id ); // new the list of values

    if ( stable.size() < 1 ) {
      stable.add( new ArrayList<MyToken>() );
    } // if

    where = Find( id );
    if ( where != null ) { // already defined, now updates the stable
      stable.get( stable.size() - 1 ).remove( where );
      stable.get( stable.size() - 1 ).add( id );
      where = null;
      if ( print ) {
        if ( !id.mfunc )
          System.out.println( "> New definition of " + id.mname + " entered ..." );
        else 
          System.out.println( "> New definition of " + id.mname + "() entered ..." );
      } // if 

    } // if
    else { // first time defined, now add  to stable
      stable.get( stable.size() - 1 ).add( new MyToken( id ) );
      if ( print ) {
        if ( !id.mfunc )
          System.out.println( "> Definition of " + id.mname + " entered ..." );
        else 
          System.out.println( "> Definition of " + id.mname + "() entered ..." );
      } // if 

    } // else
    
    while ( token.mtype.equals( "," ) ) {
      token = GetToken(); // ,
      id = GetToken(); // id
      id.mindex = 0;
      token = PeekToken();
      if ( token.mtype.equals( "[" ) ) {
        token = GetToken(); // [
        token = GetToken(); // CONSTANT
        id.mlength = Integer.parseInt( token.mname );
        token = GetToken(); // ]
        token = PeekToken();
      } // if
      
      Addmlist( id ); // new the list of id values

      if ( stable.size() < 1 ) {
        stable.add( new ArrayList<MyToken>() );
      } // if

      where = Find( id );
      if ( where != null ) { // already defined, now updates the stable
        stable.get( stable.size() - 1 ).remove( where );
        stable.get( stable.size() - 1 ).add( id );
        if ( print ) {
          if ( !id.mfunc )
            System.out.println( "New definition of " + id.mname + " entered ..." );
          else 
            System.out.println( "New definition of " + id.mname + "() entered ..." );
        } // if 

        where = null;
      } // if
      else { // first time defined, now add  to stable
        stable.get( stable.size() - 1 ).add( new MyToken( id ) );
        if ( print ) {
          if ( !id.mfunc )
            System.out.println( "Definition of " + id.mname + " entered ..." );
          else 
            System.out.println( "Definition of " + id.mname + "() entered ..." );
        } // if

      } // else
      
    } // while 
    
    GetToken(); // ;
    
  } // Rest_of_declarators()
  
  void Rest_of_declarators_noexe() {
    MyToken token = PeekToken();
    
    if ( token.mtype.equals( "[" ) ) {
      token = GetToken(); // [
      token = GetToken(); // CONSTANT
      token = GetToken(); // ]
      token = PeekToken();
    } // if

    while ( token.mtype.equals( "," ) ) {
      token = GetToken(); // ,
      GetToken(); // id
      
      token = PeekToken();
      if ( token.mtype.equals( "[" ) ) {
        token = GetToken(); // [
        token = GetToken(); // CONSTANT
        token = GetToken(); // ]
        token = PeekToken();
      } // if
      
    } // while 
    
    GetToken(); // ;
  } // Rest_of_declarators_noexe()
  
  void Function_definition_without_id() {
    ArrayList<MyToken> parameter = new ArrayList<MyToken>();
    MyToken token = GetToken(); // (
    token = PeekToken();
    
    if ( token.mtype.equals( "VOID" ) ) {
      token = GetToken();
    } // if
    else if ( token.mtype.equals( "INT" ) || token.mtype.equals( "CHAR" ) ||
              token.mtype.equals( "FLOAT" ) || token.mtype.equals( "STRING" ) ||
              token.mtype.equals( "BOOL" ) ) {
      parameter = Formal_parameter_list();
      
    } // else if
    
    GetToken(); // )
    
    Compound_statement( parameter );
    
  } // Function_definition_without_id()
  
  ArrayList<MyToken> Formal_parameter_list() {
    // need pass the parameter to compound_statement
    ArrayList<MyToken> parameter = new ArrayList<MyToken>();
    MyToken token = GetToken(); // type_specifier
    token = PeekToken();
    
    if ( token.mtype.equals( "&" ) ) {
      GetToken();
      token = PeekToken();
    } // if
    
    MyToken id = GetToken(); // id
    token = PeekToken();
    
    if ( token.mtype.equals( "[" ) ) {
      token = GetToken(); // [
      token = GetToken(); // CONSTANT
      id.mindex = Integer.parseInt( token.mname );
      Addmlist( id );
      token = GetToken(); // ]
      token = PeekToken();
    } // if
    
    parameter.add( id );

    while ( token.mtype.equals( "," ) ) {
      token = GetToken(); // ,
      token = GetToken(); // type_specifier
      token = PeekToken();
      
      if ( token.mtype.equals( "&" ) ) {
        GetToken();
        token = PeekToken();
      } // if
      
      id = GetToken(); // id
      
      token = PeekToken();
      if ( token.mtype.equals( "[" ) ) {
        token = GetToken(); // [
        token = GetToken(); // CONSTANT
        id.mindex = Integer.parseInt( token.mname );
        Addmlist( id );
        token = GetToken(); // ]
        token = PeekToken();
      } // if
      
      parameter.add( id );
      
    } // while
    
    
    return parameter;
  } // Formal_parameter_list()
  
  void Compound_statement( ArrayList<MyToken> parameter ) {
    stable.add( new ArrayList<MyToken>() );
    for ( int i = 0; parameter != null && i < parameter.size() ; i++ ) {
      stable.get( stable.size() - 1 ).add( parameter.get( i ) );
    } // for
    
    MyToken token = GetToken(); // {
    token = PeekToken();
    
    while ( !token.mtype.equals( "}" ) ) {
      if ( token.mtype.equals( "INT" ) || token.mtype.equals( "CHAR" ) ||
           token.mtype.equals( "FLOAT" ) || token.mtype.equals( "STRING" ) ||
           token.mtype.equals( "BOOL" ) ) {
        Declaration(); 
      } // if
      else {
        Statement();
      } // else
      
      token = PeekToken();
    } // while
    
    token = GetToken(); // }
    stable.remove( stable.size() - 1 );
  } // Compound_statement()
  
  void Compound_statement_noexe() {    
    MyToken token = GetToken(); // {
    token = PeekToken();
    
    while ( !token.mtype.equals( "}" ) ) {
      if ( token.mtype.equals( "INT" ) || token.mtype.equals( "CHAR" ) ||
           token.mtype.equals( "FLOAT" ) || token.mtype.equals( "STRING" ) ||
           token.mtype.equals( "BOOL" ) ) {
        Declaration_noexe(); 
      } // if
      else {
        Statement_noexe();
      } // else
      
      token = PeekToken();
    } // while
    
    GetToken(); // }
  } // Compound_statement_noexe()
  
  void Declaration() {
    GetToken(); // type_specifer
    MyToken id = GetToken(); // id
    Rest_of_declarators( id, false );
  } // Declaration()
  
  void Declaration_noexe() {
    GetToken(); // type_specifer
    MyToken id = GetToken(); // id
    Rest_of_declarators_noexe();
  } // Declaration_noexe()
  
  String GetValue( MyToken token ) {
    String ans;
    if ( token.mtype.equals( "IDENTIFIER" ) ) {
      ans = token.mlist.get( token.mindex );
    } // if
    else {
      ans = token.mname;
    } // else
    
    return ans;
  } // GetValue()
  
  void Statement() {
    MyToken token = PeekToken();
    if ( token.mtype.equals( ";" ) ) {
      token = GetToken();
    } // if
    else if ( token.mtype.equals( "IDENTIFIER" ) || token.mtype.equals( "PP" ) || 
              token.mtype.equals( "MM" ) || token.mtype.equals( "-" ) ||
              token.mtype.equals( "+" ) || token.mtype.equals( "!" ) ||
              token.mtype.equals( "CONSTANT" ) ) {
      Expression();
      token = GetToken(); // ;
    } // else if 
    else if ( token.mtype.equals( "RETURN" ) ) {
      token = GetToken(); // Return
      token = GetToken(); // [
      Expression();
      token = GetToken(); // ]
      token = GetToken(); // ;
    } // else if
    else if ( token.mtype.equals( "{" ) ) {
      ArrayList<MyToken> trash = null;
      Compound_statement( trash );
    } // else if 
    else if ( token.mtype.equals( "IF" ) ) {
      token = GetToken(); // if
      token = GetToken(); // (
      token = Expression();
      GetToken(); // )
      
      // "true" , bool a = true, a > b
      if ( token.mrtype.equals( "TRUE" ) || token.mname.equals( "true" ) ||
           ( token.mtype.equals( "IDENTIFIER" ) && 
             token.mlist.get( token.mindex ).equals( "true" ) ) ) {
        Statement();
        token = PeekToken();
        if ( token.mtype.equals( "ELSE" ) ) {
          token = GetToken();
          Statement_noexe();
        } // if
        
      } // if 
      else {
        Statement_noexe();
        token = PeekToken();
        if ( token.mtype.equals( "ELSE" ) ) {
          token = GetToken();
          Statement();
        } // if 
        
      } // else 
      
    } // else if
    else if ( token.mtype.equals( "WHILE" ) ) {
      token = GetToken(); // WHILE
      token = GetToken(); // (
      int cond = sindex;
      token = Expression();
      GetToken(); // )
      
      while ( token.mrtype.equals( "TRUE" ) || token.mname.equals( "true" ) ||
              ( token.mtype.equals( "IDENTIFIER" ) && 
                token.mlist.get( token.mindex ).equals( "true" ) ) ) {
        Statement();
        sindex = cond;
        token = Expression();
        GetToken(); // )
      } // while 
      
      Statement_noexe();
    } // else if
    else {
      token = GetToken(); // DO
      int cond = sindex;
      Statement();
      token = GetToken(); // While
      token = GetToken(); // (
      token = Expression();
      GetToken(); // )
      
      while ( token.mrtype.equals( "TRUE" ) || token.mrtype.equals( "true" ) ||
              ( token.mtype.equals( "IDENTIFIER" ) && 
                token.mlist.get( token.mindex ).equals( "true" ) ) ) {
        sindex = cond;
        Statement();
        GetToken(); // While
        GetToken(); // (
        token = Expression();
        GetToken(); // )
      } // while 
      
      GetToken(); // ;
    } // else
    
  } // Statement()
  
  void Statement_noexe() {
    MyToken token = PeekToken();
    if ( token.mtype.equals( ";" ) ) {
      token = GetToken();
    } // if
    else if ( token.mtype.equals( "IDENTIFIER" ) || token.mtype.equals( "PP" ) || 
              token.mtype.equals( "MM" ) || token.mtype.equals( "-" ) ||
              token.mtype.equals( "+" ) || token.mtype.equals( "!" ) ||
              token.mtype.equals( "CONSTANT" ) ) {
      Expression_noexe();
      token = GetToken(); // ;
    } // else if 
    else if ( token.mtype.equals( "RETURN" ) ) {
      token = GetToken(); // Return
      token = GetToken(); // [
      Expression_noexe();
      token = GetToken(); // ]
      token = GetToken(); // ;
    } // else if
    else if ( token.mtype.equals( "{" ) ) {
      ArrayList<MyToken> trash = null;
      Compound_statement_noexe();
    } // else if 
    else if ( token.mtype.equals( "IF" ) ) {
      token = GetToken(); // if
      token = GetToken(); // (
      Expression_noexe();
      token = GetToken(); // )
      Statement_noexe();
      token = PeekToken();
      if ( token.mtype.equals( "ELSE" ) ) {
        token = GetToken();
        Statement_noexe();
      } // if 
    } // else if
    else if ( token.mtype.equals( "WHILE" ) ) {
      token = GetToken(); // WHILE
      token = GetToken(); // (
      Expression_noexe();
      token = GetToken(); // )
      Statement_noexe();
    } // else if
    else {
      token = GetToken(); // DO
      Statement_noexe();
      token = GetToken(); // While
      token = GetToken(); // (
      Expression_noexe();
      token = GetToken(); // )
      token = GetToken(); // ;
      
    } // else
    
  } // Statement_noexe()
  
  MyToken Expression() {
    MyToken id = Basic_expression();
    MyToken token = PeekToken();
    
    while ( token.mtype.equals( "," ) ) {
      GetToken();
      id = Basic_expression();  
      token = PeekToken();
    } // while
    
    return id;
  } // Expression()
  
  void Expression_noexe() {
    Basic_expression_noexe();
    MyToken token = PeekToken();
    
    while ( token.mtype.equals( "," ) ) {
      GetToken();
      Basic_expression_noexe();  
      token = PeekToken();
    } // while
    
  } // Expression_noexe()
  
  MyToken Basic_expression() {
    MyToken token = PeekToken();
    MyToken id = new MyToken();
    
    if ( token.mtype.equals( "IDENTIFIER" ) ) {
      token = GetToken(); // id
      MyToken temp = new MyToken( Find( token ) ) ;
      id = Rest_of_identifier_started_basic_exp( temp ); // finish id
    } // if
    else if ( token.mtype.equals( "PP" ) || token.mtype.equals( "MM" ) ) {
      MyToken pm = GetToken(); // PP || MM
      token = GetToken(); // id
      MyToken temp = new MyToken( Find( token ) ) ;
      id = Rest_of_ppmm_identifier_started_basic_exp( pm, temp );
      
    } // else if 
    else if ( token.mtype.equals( "+" ) || token.mtype.equals( "-" ) ||
              token.mtype.equals( "!" ) ) {
      int sign = 1; // +: +, -: - 
      
      while ( token.mtype.equals( "+" ) || token.mtype.equals( "-" ) ||
              token.mtype.equals( "!" ) ) {
        token = GetToken();
        if ( token.mtype.equals( "+" ) ) {
          sign *= 1;
        } // if
        else if ( token.mtype.equals( "-" ) || token.mtype.equals( "!" ) ) {
          sign *= -1;
        } // else if
        
        token = PeekToken();
      } // while         
        
      token = Signed_unary_exp( sign ); // constant or var 
      id = Romce_and_romloe( token );
            
    } // else if
    else {
      if ( token.mtype.equals( "CONSTANT" ) ) {
        token = GetToken();
      } // if
      else {
        GetToken();
        token = new MyToken( Expression() );
        GetToken();
      } // else 
      
      id = Romce_and_romloe( token );
      
    } // else
    
    return id;
  } // Basic_expression()
  
  void Basic_expression_noexe() {
    MyToken token = PeekToken();    
    if ( token.mtype.equals( "IDENTIFIER" ) ) {
      token = GetToken(); // id
      Rest_of_identifier_started_basic_exp_noexe(); // finish id
    } // if
    else if ( token.mtype.equals( "PP" ) || token.mtype.equals( "MM" ) ) {
      GetToken(); // PP || MM
      GetToken(); // id
      Rest_of_ppmm_identifier_started_basic_exp_noexe();
      
    } // else if 
    else if ( token.mtype.equals( "+" ) || token.mtype.equals( "-" ) ||
              token.mtype.equals( "!" ) ) {
      while ( token.mtype.equals( "+" ) || token.mtype.equals( "-" ) ||
              token.mtype.equals( "!" ) ) {
        GetToken();       
        token = PeekToken();
      } // while         
        
      Signed_unary_exp_noexe(); // constant or var 
      Romce_and_romloe_noexe();
            
    } // else if
    else {
      if ( token.mtype.equals( "CONSTANT" ) ) {
        token = GetToken();
      } // if
      else {
        GetToken();
        Expression_noexe();
        GetToken();
      } // else 
      
      Romce_and_romloe_noexe();
      
    } // else

  } // Basic_expression_noexe()
  
  MyToken Rest_of_identifier_started_basic_exp( MyToken paid ) {
    MyToken token = PeekToken();
    
    if ( token.mtype.equals( "(" ) ) {
      GetToken(); // (
      ArrayList<MyToken> parameter = Actual_parameter_list();
      GetToken(); // )
      paid = Romce_and_romloe( paid );
    } // if
    else {
      if ( token.mtype.equals( "[" ) ) {
        GetToken(); // [
        token = Expression();
        if ( token.mtype.equals( "IDENTIFIER" ) ) {
          paid.mindex = Integer.parseInt( GetValue( token ) );
        } // if 
        else {
          paid.mindex = Integer.parseInt( token.mname );
        } // else 
        
        GetToken(); // ]
        token = PeekToken();
      } // if
      
      if ( token.mtype.equals( "=" ) || token.mtype.equals( "TE" ) ||
           token.mtype.equals( "DE" ) || token.mtype.equals( "RE" ) ||
           token.mtype.equals( "PE" ) || token.mtype.equals( "ME" ) ) {
        MyToken operate = GetToken();
        token = Basic_expression(); // postitem
        
        if ( operate.mtype.equals( "=" ) ) {
          
          if ( paid.mrtype.equals( "INT" ) ) {
            float temp = Float.parseFloat( GetValue( token ) );
            int temp2 = ( int ) temp;
            String ans = Integer.toString( temp2 );
            
            if ( paid.mtype.equals( "IDENTIFIER" ) ) {
              paid.mlist.set( paid.mindex, ans );
            } // if
            else if ( paid.mtype.equals( "CONSTANT" ) ) {
              paid.mname = ans;
            } // else if
            
          } // if
          else {
            if ( paid.mtype.equals( "IDENTIFIER" ) ) {
              paid.mlist.set( paid.mindex, GetValue( token ) );
            } // if
            else if ( paid.mtype.equals( "CONSTANT" ) ) {
              paid.mname = GetValue( token );
            } // else if
          } // else 
                   
        } // if
        else if ( operate.mtype.equals( "TE" ) ) {
          float pre = Float.parseFloat( paid.mlist.get( paid.mindex ) );
          float post = Float.parseFloat( GetValue( token ) );
          float ans = pre * post;
          
          if ( paid.mrtype.equals( "FLOAT" ) ) { // id is float 
            paid.mlist.set( paid.mindex, Float.toString( ans ) );
          } // if 
          else {
            int temp = ( int ) ans;
            paid.mlist.set( paid.mindex, Integer.toString( temp ) );
          } // else 
          
        } // else if 
        else if ( operate.mtype.equals( "DE" ) ) {
          float pre = Float.parseFloat( paid.mlist.get( paid.mindex ) );
          float post = Float.parseFloat( GetValue( token ) );
          float ans = pre / post;
          
          if ( paid.mrtype.equals( "FLOAT" ) ) { // id is float 
            paid.mlist.set( paid.mindex, Float.toString( ans ) );
          } // if 
          else {
            int temp = ( int )  ans;
            paid.mlist.set( paid.mindex, Integer.toString( temp ) );
          } // else 
          
        } // else if 
        else if ( operate.mtype.equals( "RE" ) ) {
          float pre = Float.parseFloat( paid.mlist.get( paid.mindex ) );
          float post = Float.parseFloat( GetValue( token ) );
          float ans = pre % post;
          
          if ( paid.mrtype.equals( "FLOAT" ) ) { // id is float 
            paid.mlist.set( paid.mindex, Float.toString( ans ) );
          } // if 
          else {
            int temp = ( int ) ans;
            paid.mlist.set( paid.mindex, Integer.toString( temp ) );
          } // else 
          
        } // else if
        else if ( operate.mtype.equals( "PE" ) ) {
          
          if ( paid.mrtype.equals( "STRING" ) ) {
            String ans = "";
            String prestr, poststr;
            if ( paid.mtype.equals( "IDENTIFIER" ) ) {
              prestr = paid.mlist.get( paid.mindex );
            } // if
            else {
              prestr = paid.mname;
            } // else 
              
            if ( token.mtype.equals( "IDENTIFIER" ) ) {
              poststr = token.mlist.get( token.mindex );
            } // if
            else {
              poststr = token.mname;
            } // else 
              
            ans = prestr + poststr;
            paid.mlist.set( paid.mindex, ans );
          } // if
          else {
            float pre = Float.parseFloat( paid.mlist.get( paid.mindex ) );
            float post = Float.parseFloat( GetValue( token ) );
            float ans = pre + post;
            
            if ( paid.mrtype.equals( "FLOAT" ) ) { // id is float 
              paid.mlist.set( paid.mindex, Float.toString( ans ) );
            } // if 
            else {
              int temp = ( int ) ans;
              paid.mlist.set( paid.mindex, Integer.toString( temp ) );
            } // else 
          } // else 
                   
        } // else if
        else if ( operate.mtype.equals( "ME" ) ) {
          float pre = Float.parseFloat( paid.mlist.get( paid.mindex ) );
          float post = Float.parseFloat( GetValue( token ) );
          float ans = pre - post;
          
          if ( paid.mrtype.equals( "FLOAT" ) ) { // id is float 
            paid.mlist.set( paid.mindex, Float.toString( ans ) );
          } // if 
          else {
            int temp = ( int ) ans;
            paid.mlist.set( paid.mindex, Integer.toString( temp ) );
          } // else  
          
        } // else if
        
        MyToken change = Find( paid );
        change.mlist.set( paid.mindex, paid.mlist.get( paid.mindex ) );
        change.mindex = paid.mindex;
        change = new MyToken( Find( change ) );
        
        return change;
      } // if
      else { // PP MM
        MyToken change = null;
        if ( token.mtype.equals( "PP" ) ) {
          token = GetToken();
          if ( paid.mrtype.equals( "FLOAT" ) ) {
            float pre = Float.parseFloat( paid.mlist.get( paid.mindex ) );
            float ans = pre + 1;
            paid.mlist.set( paid.mindex, Float.toString( ans ) );
          } // if
          else {
            int pre = Integer.parseInt( paid.mlist.get( paid.mindex ) );
            int ans = pre + 1;
            paid.mlist.set( paid.mindex, Integer.toString( ans ) );
          } // else
          
          change = Find( paid );
          change.mlist.set( paid.mindex, paid.mlist.get( paid.mindex ) );
          change.mindex = paid.mindex;
          
          if ( change.mrtype.equals( "FLOAT" ) ) {
            float nowid = Float.parseFloat( change.mlist.get( change.mindex ) ) - 1;
            paid.mlist.set( change.mindex, Float.toString( nowid ) ); 
          } // if
          else if ( change.mrtype.equals( "INT" ) ) {
            int nowid = Integer.parseInt( change.mlist.get( change.mindex ) ) - 1;
            paid.mlist.set( change.mindex, Integer.toString( nowid ) ); 
          } // else if
          
        } // if
        else if ( token.mtype.equals( "MM" ) ) {
          token = GetToken();
          if ( paid.mrtype.equals( "FLOAT" ) ) {
            float pre = Float.parseFloat( paid.mlist.get( paid.mindex ) );
            float ans = pre - 1;
            paid.mlist.set( paid.mindex, Float.toString( ans ) );
            paid.mdot = true;
          } // if
          else {
            int pre = Integer.parseInt( paid.mlist.get( paid.mindex ) );
            int ans = pre - 1;
            paid.mlist.set( paid.mindex, Integer.toString( ans ) );
          } // else
          
          
          change = Find( paid );
          change.mlist.set( paid.mindex, paid.mlist.get( paid.mindex ) );
          change.mindex = paid.mindex;
          
          if ( change.mrtype.equals( "FLOAT" ) ) {
            float nowid = Float.parseFloat( change.mlist.get( change.mindex ) ) + 1;
            paid.mlist.set( change.mindex, Float.toString( nowid ) ); 
          } // if
          else if ( change.mrtype.equals( "INT" ) ) {
            int nowid = Integer.parseInt( change.mlist.get( change.mindex ) ) + 1;
            paid.mlist.set( change.mindex, Integer.toString( nowid ) ); 
          } // else if 
          
        } // else if
        
        paid = Romce_and_romloe( paid );   
        
      } // else
      
    } // else
    
    return paid;
  } // Rest_of_identifier_started_basic_exp()
  
  void Rest_of_identifier_started_basic_exp_noexe() {
    MyToken token = PeekToken();
    if ( token.mtype.equals( "(" ) ) {
      GetToken(); // (
      Actual_parameter_list_noexe();
      GetToken(); // )
      Romce_and_romloe_noexe();
    } // if
    else {
      if ( token.mtype.equals( "[" ) ) {
        GetToken(); // [
        Expression_noexe();
        GetToken(); // ]
        token = PeekToken();
      } // if
      
      if ( token.mtype.equals( "=" ) || token.mtype.equals( "TE" ) ||
           token.mtype.equals( "DE" ) || token.mtype.equals( "RE" ) ||
           token.mtype.equals( "PE" ) || token.mtype.equals( "ME" ) ) {
        MyToken operate = GetToken();
        Basic_expression_noexe(); // postitem
      } // if
      else { // PP MM
        MyToken change = null;
        if ( token.mtype.equals( "PP" ) ) {
          token = GetToken();
        } // if 
        else if ( token.mtype.equals( "MM" ) ) {
          token = GetToken();          
        } // else if
        
        Romce_and_romloe_noexe();   
        
      } // else
      
    } // else

  } // Rest_of_identifier_started_basic_exp_noexe()
  
  MyToken Rest_of_ppmm_identifier_started_basic_exp( MyToken pm, MyToken paid ) {
    MyToken token = PeekToken();
    MyToken retuid = null;
    
    if ( token.mtype.equals( "[" ) ) {
      GetToken(); // [
      token = Expression();
      paid.mindex = Integer.parseInt( GetValue( token ) );
      GetToken(); // ]
    } // if
    
    if ( pm.mtype.equals( "PP" ) ) {
      float pre = Float.parseFloat( paid.mlist.get( paid.mindex ) );
      float ans = pre + 1;
      
      if ( paid.mrtype.equals( "FLOAT" ) ) {
        paid.mlist.set( paid.mindex, Float.toString( ans ) );
      } // if
      else {
        int temp = ( int ) ans;
        paid.mlist.set( paid.mindex, Integer.toString( temp ) );
      } // else 
      
    } // if
    else if ( pm.mtype.equals( "MM" ) ) {
      float pre = Float.parseFloat( paid.mlist.get( paid.mindex ) );
      float ans = pre - 1;
     
      if ( paid.mrtype.equals( "FLOAT" ) ) {
        paid.mlist.set( paid.mindex, Float.toString( ans ) );
      } // if
      else {
        int temp = ( int ) ans;
        paid.mlist.set( paid.mindex, Integer.toString( temp ) );
      } // else 
      
    } // else if

    MyToken change = Find( paid );
    change.mlist.set( paid.mindex, paid.mlist.get( paid.mindex ) );
    change.mindex = paid.mindex;
    
    change = new MyToken( Find( change ) );
    
    retuid = Romce_and_romloe( change );
    
    return retuid;
  } // Rest_of_ppmm_identifier_started_basic_exp()
  
  void Rest_of_ppmm_identifier_started_basic_exp_noexe() {
    MyToken token = PeekToken();
    if ( token.mtype.equals( "[" ) ) {
      GetToken(); // [
      Expression_noexe();
      GetToken(); // ]
    } // if
    
    Romce_and_romloe_noexe();
  } // Rest_of_ppmm_identifier_started_basic_exp_noexe()
  
  ArrayList<MyToken> Actual_parameter_list() {
    ArrayList<MyToken> parameter = new ArrayList<MyToken>();
    MyToken token = Basic_expression();
    parameter.add( new MyToken( token ) );
    token = PeekToken();
    
    while ( token.mtype.equals( "," ) ) {
      GetToken();
      token = Basic_expression();  
      parameter.add( new MyToken( token ) );
      token = PeekToken();
    } // while
    
    return parameter;
  } // Actual_parameter_list()
  
  void Actual_parameter_list_noexe() {
    ArrayList<MyToken> parameter = new ArrayList<MyToken>();
    Basic_expression_noexe();
    MyToken token = PeekToken();
    
    while ( token.mtype.equals( "," ) ) {
      GetToken();
      Basic_expression();  
      token = PeekToken();
    } // while

  } // Actual_parameter_list_noexe()
  
  MyToken Romce_and_romloe( MyToken paid ) {
    MyToken cond = Rest_of_maybe_logical_or_exp( paid );
    MyToken token = PeekToken();
    if ( token.mtype.equals( "?" ) ) {
      GetToken(); // ?
      
      // "true" , bool a = true, a > b
      if ( cond.mname.equals( "true" ) || cond.mlist.get( cond.mindex ).equals( "true" )  ) {
        MyToken retu1 = Basic_expression();
        GetToken(); // :
        Basic_expression_noexe();
        return retu1;
      } // if 
      else { // false
        Basic_expression_noexe();
        GetToken(); // :
        MyToken retu2 = Basic_expression();
        return retu2;
      } // else 
      
    } // if 

    return cond;
  } // Romce_and_romloe()
  
  void Romce_and_romloe_noexe() {
    Rest_of_maybe_logical_or_exp_noexe();
    MyToken token = PeekToken();
    if ( token.mtype.equals( "?" ) ) {
      GetToken(); // ?
      Basic_expression_noexe();
      GetToken(); // :
      Basic_expression_noexe();
    } // if 

  } // Romce_and_romloe_noexe()
  
  MyToken Rest_of_maybe_logical_or_exp( MyToken paid ) {
    MyToken retuid = Rest_of_maybe_logical_and_exp( paid );
    MyToken token = PeekToken();
    while ( token.mtype.equals( "OR" ) ) {
      boolean retuidbool = false;
      if ( retuid.mrtype.equals( "TRUE" ) || retuid.mrtype.equals( "FALSE"  ) ) {
        retuidbool = Boolean.parseBoolean( retuid.mname ); 
      } // if
      else {
        retuidbool = Boolean.parseBoolean( retuid.mlist.get( retuid.mindex ) );
      } // else

      GetToken(); // and
      MyToken postitem = Maybe_bit_or_exp();
      
      
      boolean postidbool = false;
      if ( postitem.mrtype.equals( "TRUE" ) || postitem.mrtype.equals( "FALSE" ) ) {
        postidbool = Boolean.parseBoolean( postitem.mname ); 
      } // if
      else {
        if ( postitem.mtype.equals( "IDENTIFIER" ) ) {
          postidbool = Boolean.parseBoolean( postitem.mlist.get( postitem.mindex ) );
        } // if
        else {
          postidbool = Boolean.parseBoolean( postitem.mname );
        } // else 
        
      } // else
      
      retuidbool = retuidbool || postidbool;
      
      if ( retuid.mrtype.equals( "TRUE" ) || retuid.mrtype.equals( "FALSE" ) ) {
        retuid.mname = Boolean.toString( retuidbool );
      } // if
      else {
        retuid.mlist.set( retuid.mindex, Boolean.toString( retuidbool ) );
      } // else
      
      token = PeekToken();
    } // while 

    return retuid;
  } // Rest_of_maybe_logical_or_exp()
  
  void Rest_of_maybe_logical_or_exp_noexe() {
    Rest_of_maybe_logical_and_exp_noexe();
    MyToken token = PeekToken();
    while ( token.mtype.equals( "OR" ) ) {
      GetToken(); // and
      Maybe_bit_or_exp_noexe();
      token = PeekToken();
    } // while 
    
  } // Rest_of_maybe_logical_or_exp_noexe()
  
  MyToken Maybe_logical_and_exp() {
    MyToken retuid = Maybe_bit_or_exp();
    MyToken token = PeekToken();
    while ( token.mtype.equals( "AND" ) ) {
      boolean retuidbool = false;
      if ( retuid.mrtype.equals( "TRUE" ) || retuid.mrtype.equals( "FALSE"  ) ) {
        retuidbool = Boolean.parseBoolean( retuid.mname ); 
      } // if
      else {
        retuidbool = Boolean.parseBoolean( retuid.mlist.get( retuid.mindex ) );
      } // else

      GetToken(); // and
      MyToken postitem = Maybe_bit_or_exp();
      
      
      boolean postidbool = false;
      if ( postitem.mrtype.equals( "TRUE" ) || postitem.mrtype.equals( "FALSE" ) ) {
        postidbool = Boolean.parseBoolean( postitem.mname ); 
      } // if
      else {
        postidbool = Boolean.parseBoolean( postitem.mlist.get( postitem.mindex ) );
      } // else
      
      retuidbool = retuidbool && postidbool;
      
      if ( retuid.mrtype.equals( "TRUE" ) || retuid.mrtype.equals( "FALSE" ) ) {
        retuid.mname = Boolean.toString( retuidbool );
      } // if
      else {
        retuid.mlist.set( retuid.mindex, Boolean.toString( retuidbool ) );
      } // else
      
      token = PeekToken();
    } // while 
    
    return retuid;
  } // Maybe_logical_and_exp()
  
  void Maybe_logical_and_exp_noexe() {
    Maybe_bit_or_exp_noexe();
    MyToken token = PeekToken();
    while ( token.mtype.equals( "AND" ) ) {
      GetToken(); // and
      Maybe_bit_or_exp_noexe();
      token = PeekToken();
    } // while 

  } // Maybe_logical_and_exp_noexe()
  
  MyToken Rest_of_maybe_logical_and_exp( MyToken paid ) {
    MyToken retuid = Rest_of_maybe_bit_or_exp( paid );
    MyToken token = PeekToken();
    while ( token.mtype.equals( "AND" ) ) {
      boolean retuidbool = false;
      if ( retuid.mrtype.equals( "TRUE" ) || retuid.mrtype.equals( "FALSE"  ) ) {
        retuidbool = Boolean.parseBoolean( retuid.mname ); 
      } // if
      else {
        retuidbool = Boolean.parseBoolean( retuid.mlist.get( retuid.mindex ) );
      } // else

      GetToken(); // and
      MyToken postitem = Maybe_bit_or_exp();
      
      
      boolean postidbool = false;
      if ( postitem.mrtype.equals( "TRUE" ) || postitem.mrtype.equals( "FALSE" ) ) {
        postidbool = Boolean.parseBoolean( postitem.mname ); 
      } // if
      else {
        postidbool = Boolean.parseBoolean( postitem.mlist.get( postitem.mindex ) );
      } // else
      
      retuidbool = retuidbool && postidbool;
      
      if ( retuid.mrtype.equals( "TRUE" ) || retuid.mrtype.equals( "FALSE" ) ) {
        retuid.mname = Boolean.toString( retuidbool );
      } // if
      else {
        retuid.mlist.set( retuid.mindex, Boolean.toString( retuidbool ) );
      } // else
      
      token = PeekToken();
    } // while 
    
    return retuid;
  } // Rest_of_maybe_logical_and_exp()
  
  void Rest_of_maybe_logical_and_exp_noexe() {
    Rest_of_maybe_bit_or_exp_noexe();
    MyToken token = PeekToken();
    while ( token.mtype.equals( "AND" ) ) {      
      GetToken(); // and
      Maybe_bit_or_exp_noexe();
      token = PeekToken();
    } // while 
    
  } // Rest_of_maybe_logical_and_exp_noexe()
  
  MyToken Maybe_bit_or_exp() {
    MyToken retuid = Maybe_bit_ex_or_exp();
    MyToken token = PeekToken();
    while ( token.mtype.equals( "|" ) ) {
      boolean retuidbool = Boolean.parseBoolean( GetValue( retuid ) );
      GetToken(); // and
      MyToken postitem = Maybe_bit_ex_or_exp();
      boolean postidbool = Boolean.parseBoolean( GetValue( postitem ) );
      // retuidbool = retuidbool | postidbool;
      retuid.mlist.set( retuid.mindex, Boolean.toString( retuidbool ) );
      token = PeekToken();
    } // while 
    
    return retuid;
  } // Maybe_bit_or_exp()
  
  void Maybe_bit_or_exp_noexe() {
    Maybe_bit_ex_or_exp_noexe();
    MyToken token = PeekToken();
    while ( token.mtype.equals( "|" ) ) {
      GetToken(); // and
      Maybe_bit_ex_or_exp_noexe();
      token = PeekToken();
    } // while 

  } // Maybe_bit_or_exp_noexe()
  
  MyToken Rest_of_maybe_bit_or_exp( MyToken paid ) {
    MyToken retuid = Rest_of_maybe_bit_ex_or_exp( paid );
    MyToken token = PeekToken();
    while ( token.mtype.equals( "|" ) ) {
      boolean retuidbool = Boolean.parseBoolean( retuid.mlist.get( retuid.mindex ) );
      GetToken(); // and
      MyToken postitem = Maybe_bit_ex_or_exp();
      boolean postidbool = Boolean.parseBoolean( GetValue( postitem ) );
      // retuidbool = retuidbool | postidbool;
      retuid.mlist.set( retuid.mindex, Boolean.toString( retuidbool ) );
      token = PeekToken();
    } // while 
    
    return retuid;
  } // Rest_of_maybe_bit_or_exp()
  
  void Rest_of_maybe_bit_or_exp_noexe() {
    Rest_of_maybe_bit_ex_or_exp_noexe();
    MyToken token = PeekToken();
    while ( token.mtype.equals( "|" ) ) {
      Maybe_bit_ex_or_exp_noexe();
      token = PeekToken();
    } // while 
    
  } // Rest_of_maybe_bit_or_exp_noexe()
  
  MyToken Maybe_bit_ex_or_exp() {
    MyToken retuid = Maybe_bit_and_exp();
    MyToken token = PeekToken();
    while ( token.mtype.equals( "^" ) ) {
      boolean retuidbool = Boolean.parseBoolean( GetValue( retuid ) );
      GetToken(); // and
      MyToken postitem = Maybe_bit_and_exp();
      boolean postidbool = Boolean.parseBoolean( GetValue( postitem ) );
      // retuidbool = retuidbool ^ postidbool;
      retuid.mlist.set( retuid.mindex, Boolean.toString( retuidbool ) );
      token = PeekToken();
    } // while 
    
    return retuid;
  } // Maybe_bit_ex_or_exp()
  
  void Maybe_bit_ex_or_exp_noexe() {
    Maybe_bit_and_exp_noexe();
    MyToken token = PeekToken();
    while ( token.mtype.equals( "^" ) ) {
      GetToken(); // and
      Maybe_bit_and_exp_noexe();
      token = PeekToken();
    } // while 
    
  } // Maybe_bit_ex_or_exp_noexe()
  
  MyToken Rest_of_maybe_bit_ex_or_exp( MyToken paid ) {
    MyToken retuid = Rest_of_maybe_bit_and_exp( paid );
    MyToken token = PeekToken();
    while ( token.mtype.equals( "^" ) ) {
      boolean retuidbool = Boolean.parseBoolean( retuid.mlist.get( retuid.mindex ) );
      GetToken(); // and
      MyToken postitem = Maybe_bit_and_exp();
      boolean postidbool = Boolean.parseBoolean( GetValue( postitem ) );
      // retuidbool = retuidbool ^ postidbool;
      retuid.mlist.set( retuid.mindex, Boolean.toString( retuidbool ) );
      token = PeekToken();
    } // while 
    
    return retuid;
  } // Rest_of_maybe_bit_ex_or_exp()
  
  void Rest_of_maybe_bit_ex_or_exp_noexe() {
    Rest_of_maybe_bit_and_exp_noexe();
    MyToken token = PeekToken();
    while ( token.mtype.equals( "^" ) ) {
      GetToken(); // and
      Maybe_bit_and_exp_noexe();
      token = PeekToken();
    } // while 
    
  } // Rest_of_maybe_bit_ex_or_exp_noexe()
  
  MyToken Maybe_bit_and_exp() {
    MyToken retuid = Maybe_equality_exp();
    MyToken token = PeekToken();
    while ( token.mtype.equals( "&" ) ) {
      boolean retuidbool = Boolean.parseBoolean( GetValue( retuid ) );
      GetToken(); // and
      MyToken postitem = Maybe_equality_exp();
      boolean postidbool = Boolean.parseBoolean( GetValue( postitem ) );
      // retuidbool = retuidbool & postidbool;
      retuid.mlist.set( retuid.mindex, Boolean.toString( retuidbool ) );
      token = PeekToken();
    } // while 
    
    return retuid;
  } // Maybe_bit_and_exp()
  
  void Maybe_bit_and_exp_noexe() {
    Maybe_equality_exp_noexe();
    MyToken token = PeekToken();
    while ( token.mtype.equals( "&" ) ) {
      GetToken(); // and
      Maybe_equality_exp_noexe();
      token = PeekToken();
    } // while 

  } // Maybe_bit_and_exp_noexe()
  
  MyToken Rest_of_maybe_bit_and_exp( MyToken paid ) {
    MyToken retuid = Rest_of_maybe_equality_exp( paid );
    MyToken token = PeekToken();
    while ( token.mtype.equals( "&" ) ) {
      boolean retuidbool = Boolean.parseBoolean( retuid.mlist.get( retuid.mindex ) );
      GetToken(); // and
      MyToken postitem = Maybe_equality_exp();
      boolean postidbool = Boolean.parseBoolean( GetValue( postitem ) );
      // retuidbool = retuidbool & postidbool;
      retuid.mlist.set( retuid.mindex, Boolean.toString( retuidbool ) );
      token = PeekToken();
    } // while 
    
    return retuid;
  } // Rest_of_maybe_bit_and_exp()
  
  void Rest_of_maybe_bit_and_exp_noexe() {
    Rest_of_maybe_equality_exp_noexe();
    MyToken token = PeekToken();
    while ( token.mtype.equals( "&" ) ) {
      GetToken(); // and
      Maybe_equality_exp_noexe();
      token = PeekToken();
    } // while 
    
  } // Rest_of_maybe_bit_and_exp_noexe()
  
  MyToken Maybe_equality_exp() {
    MyToken retuid = Maybe_relational_exp();
    MyToken token = PeekToken();
    String ans;
    while ( token.mtype.equals( "EQ" ) || token.mtype.equals( "NEQ" ) ) {
      token = GetToken(); // EQ NEQ
      MyToken postitem = Maybe_relational_exp();
      float retuidfloat = Float.parseFloat( GetValue( retuid ) );
      float postfloat = Float.parseFloat( GetValue( postitem ) );
      if ( token.mtype.equals( "EQ" ) ) {
        ans = Boolean.toString( retuidfloat == postfloat );
      } // if 
      else {
        ans = Boolean.toString( retuidfloat != postfloat );
      } // else
      
      if ( retuid.mtype.equals( "IDENTIFIER" ) ) {
        retuid.mlist.set( retuid.mindex, ans );
      } // if
      else {
        retuid.mname = ans;
      } // else 
      
      
      token = PeekToken();
    } // while 
    
    return retuid;
  } // Maybe_equality_exp()
  
  void Maybe_equality_exp_noexe() {
    Maybe_relational_exp_noexe();
    MyToken token = PeekToken();
    while ( token.mtype.equals( "EQ" ) || token.mtype.equals( "NEQ" ) ) {
      token = GetToken(); // EQ NEQ
      Maybe_relational_exp_noexe();
      token = PeekToken();
    } // while 
    
  } // Maybe_equality_exp_noexe()
  
  MyToken Rest_of_maybe_equality_exp( MyToken paid ) {
    MyToken retuid = Rest_of_maybe_relational_exp( paid );
    MyToken token = PeekToken();
    String ans;
    while ( token.mtype.equals( "EQ" ) || token.mtype.equals( "NEQ" ) ) {
      token = GetToken(); // EQ NEQ
      float retuidfloat = Float.parseFloat( GetValue( retuid ) );
      MyToken postitem = Maybe_relational_exp();
      float postfloat = Float.parseFloat( GetValue( postitem ) );
      if ( token.mtype.equals( "EQ" ) ) {
        ans = Boolean.toString( retuidfloat == postfloat );
      } // if 
      else {
        ans = Boolean.toString( retuidfloat != postfloat );
      } // else
      
      if ( retuid.mtype.equals( "IDENTIFIER" ) ) {
        retuid.mlist.set( retuid.mindex, ans );
      } // if
      else { // constant
        retuid.mname = ans;
      } // else 
      
      token = PeekToken();
    } // while 
    
    return retuid;
  } // Rest_of_maybe_equality_exp()
  
  void Rest_of_maybe_equality_exp_noexe() {
    Rest_of_maybe_relational_exp_noexe();
    MyToken token = PeekToken();
    while ( token.mtype.equals( "EQ" ) || token.mtype.equals( "NEQ" ) ) {
      token = GetToken(); // EQ NEQ
      Maybe_relational_exp_noexe();
      token = PeekToken();
    } // while 

  } // Rest_of_maybe_equality_exp_noexe()
  
  MyToken Maybe_relational_exp() {
    MyToken retuid = Maybe_shift_exp();
    MyToken token = PeekToken();
    String ans;
    while ( token.mtype.equals( "<" ) || token.mtype.equals( ">" ) ||
            token.mtype.equals( "LE" ) || token.mtype.equals( "GE" ) ) {
      token = GetToken(); // < > LE GE
      MyToken postitem = Maybe_shift_exp();
      float retuidfloat = Float.parseFloat( GetValue( retuid ) );
      float postfloat = Float.parseFloat( GetValue( postitem ) );
      if ( token.mtype.equals( "<" ) ) {
        ans = Boolean.toString( retuidfloat < postfloat );
      } // if 
      else if ( token.mtype.equals( ">" ) ) {
        ans = Boolean.toString( retuidfloat > postfloat );
      } // else if
      else if ( token.mtype.equals( "LE" ) ) {
        ans = Boolean.toString( retuidfloat <= postfloat );
      } // else if
      else {
        ans = Boolean.toString( retuidfloat >= postfloat );
      } // else
      
      if ( retuid.mtype.equals( "IDENTIFIER" ) ) {
        retuid.mlist.set( retuid.mindex, ans );
      } // if
      else { // constant
        retuid.mname = ans;
      } // else 
      
      token = PeekToken();
    } // while 
    
    return retuid;
  } // Maybe_relational_exp()
  
  void Maybe_relational_exp_noexe() {
    Maybe_shift_exp_noexe();
    MyToken token = PeekToken();
    while ( token.mtype.equals( "<" ) || token.mtype.equals( ">" ) ||
            token.mtype.equals( "LE" ) || token.mtype.equals( "GE" ) ) {
      token = GetToken(); // < > LE GE
      Maybe_shift_exp_noexe();
      token = PeekToken();
    } // while 
    
  } // Maybe_relational_exp_noexe()
  
  MyToken Rest_of_maybe_relational_exp( MyToken paid ) {
    MyToken retuid = Rest_of_maybe_shift_exp( paid );
    MyToken token = PeekToken();
    String ans;
    while ( token.mtype.equals( "<" ) || token.mtype.equals( ">" ) ||
            token.mtype.equals( "LE" ) || token.mtype.equals( "GE" ) ) {
      token = GetToken(); // < > LE GE
      float retuidfloat = Float.parseFloat( GetValue( retuid ) );
      MyToken postitem = Maybe_shift_exp();
      float postfloat = Float.parseFloat( GetValue( postitem ) );
      if ( token.mtype.equals( "<" ) ) {
        ans = Boolean.toString( retuidfloat < postfloat );
      } // if 
      else if ( token.mtype.equals( ">" ) ) {
        ans = Boolean.toString( retuidfloat > postfloat );
      } // else if
      else if ( token.mtype.equals( "LE" ) ) {
        ans = Boolean.toString( retuidfloat <= postfloat );
      } // else if
      else {
        ans = Boolean.toString( retuidfloat >= postfloat );
      } // else
      
      if ( retuid.mtype.equals( "IDENTIFIER" ) ) {
        retuid.mlist.set( retuid.mindex, ans );
      } // if
      else { // constant
        retuid.mname = ans;
      } // else 
      
      
      token = PeekToken();
    } // while 
    
    return retuid;
  } // Rest_of_maybe_relational_exp()
  
  void Rest_of_maybe_relational_exp_noexe() {
    Rest_of_maybe_shift_exp_noexe();
    MyToken token = PeekToken();
    while ( token.mtype.equals( "<" ) || token.mtype.equals( ">" ) ||
            token.mtype.equals( "LE" ) || token.mtype.equals( "GE" ) ) {
      token = GetToken(); // < > LE GE
      Maybe_shift_exp_noexe();      
      token = PeekToken();
    } // while 
   
  } // Rest_of_maybe_relational_exp_noexe()
  
  MyToken Maybe_shift_exp() {
    MyToken retuid = Maybe_additive_exp();
    MyToken operate = PeekToken(); // << >>
    MyToken postitem = null;
    if ( retuid.mname.equals( "cout" ) ) {
      while ( operate.mtype.equals( "LS" ) || operate.mtype.equals( "RS" ) ) {
        operate = GetToken();
        postitem = Maybe_additive_exp();
        String cout = "";
        if ( postitem.mtype.equals( "IDENTIFIER" ) ) {
          if ( postitem.mrtype.equals( "FLOAT" ) ) {
            float temp = Float.parseFloat( postitem.mlist.get( postitem.mindex ) );
            cout = String.format( "%.3f", temp );
          } // if
          else {
            cout = postitem.mlist.get( postitem.mindex );
          } // else 
          
        } // if
        else if ( postitem.mrtype.equals( "STRING" ) ) {
          for ( int i =  0; i < postitem.mname.length() ; i++ ) {
            if ( postitem.mname.charAt( i ) == '\'' && postitem.mname.charAt( i + 1 ) == '"' ) {
              cout += postitem.mname.charAt( i + 1 );
              i += 1;
            } // if
            else if ( postitem.mname.charAt( i ) != '"' ) {
              cout += postitem.mname.charAt( i );
            } // else if 
            
          } // for
          
        } // else if
        else if ( postitem.mrtype.equals( "TRUE" ) || postitem.mrtype.equals( "FALSE" ) ) {
          cout = postitem.mname;
        } // else if 
        else if ( postitem.mrtype.equals( "INT" ) || postitem.mrtype.equals( "FLOAT" ) ) {
          cout = postitem.mname;
        } // else if 
        
        for ( int i = 0; i < cout.length() ; i++ ) {
          if ( cout.charAt( i ) != '\\' ) {
            System.out.print( cout.charAt( i ) );
          } // if
          
          else if ( cout.charAt( i ) == '\\' &&
                    cout.charAt( i + 1 ) == 'n' ) {
            System.out.println();
            i += 1;
          } // else if 
          
        } // for
        

        operate = PeekToken();
      } // while
      
      return postitem;
    } // if
    else {
      while ( operate.mtype.equals( "LS" ) || operate.mtype.equals( "RS" ) ) {
        operate = GetToken();
        postitem = Maybe_additive_exp();
        int retuidint = Integer.parseInt( GetValue( retuid ) );
        int postitemint = Integer.parseInt( GetValue( postitem ) );
        if ( operate.mtype.equals( "LS" ) ) { // <<
          retuidint = retuidint << postitemint;
        } // if 
        else {
          retuidint = retuidint >> postitemint;
        } // else 
        
        if ( retuid.mtype.equals( "CONSTANT" ) ) {
          retuid.mname = Integer.toString( retuidint );
        } // if
        else {
          retuid.mlist.set( retuid.mindex, Integer.toString( retuidint ) );
        } // else
        
        operate = PeekToken();
      } // while 
      
      
    } // else 
     
    return retuid;
  } // Maybe_shift_exp()
  
  void Maybe_shift_exp_noexe() {
    Maybe_additive_exp_noexe();
    MyToken operate = PeekToken(); // << >>
    while ( operate.mtype.equals( "LS" ) || operate.mtype.equals( "RS" ) ) {
      operate = GetToken();
      Maybe_additive_exp_noexe();
      operate = PeekToken();
    } // while 

  } // Maybe_shift_exp_noexe()
  
  MyToken Rest_of_maybe_shift_exp( MyToken paid ) {
    MyToken retuid = Rest_of_maybe_additive_exp( paid );
    MyToken operate = PeekToken(); // << >>
    MyToken postitem = null;
    if ( retuid.mname.equals( "cout" ) ) {
      while ( operate.mtype.equals( "LS" ) || operate.mtype.equals( "RS" ) ) {
        operate = GetToken();
        postitem = Maybe_additive_exp();
        String cout = "";
        if ( postitem.mtype.equals( "IDENTIFIER" ) ) {
          if ( postitem.mlist.get( postitem.mindex ).equals( "true" ) ||
               postitem.mlist.get( postitem.mindex ).equals( "false" ) ) {
            cout = postitem.mlist.get( postitem.mindex );
          } // if          
          else if ( postitem.mrtype.equals( "FLOAT" ) ) {
            float temp = Float.parseFloat( postitem.mlist.get( postitem.mindex ) );
            cout = String.format( "%.3f", temp );
          } // else if
          else {
            cout = postitem.mlist.get( postitem.mindex );
          } // else 
          
        } // if
        else if ( postitem.mrtype.equals( "STRING" ) ) {
          for ( int i =  0; i < postitem.mname.length() ; i++ ) {
            if ( postitem.mname.charAt( i ) == '\'' && postitem.mname.charAt( i + 1 ) == '"' ) {
              cout += postitem.mname.charAt( i + 1 );
              i += 1;
            } // if
            else if ( postitem.mname.charAt( i ) != '"' ) {
              cout += postitem.mname.charAt( i );
            } // else if 
            
          } // for
          
        } // else if
        else if ( postitem.mrtype.equals( "TRUE" ) || postitem.mrtype.equals( "FALSE" ) ) {
          cout = postitem.mname;
        } // else if 
        else if ( postitem.mrtype.equals( "INT" ) || postitem.mrtype.equals( "FLOAT" ) ) {
          cout = postitem.mname;
        } // else if 
        
        for ( int i = 0; i < cout.length() ; i++ ) {
          if ( cout.charAt( i ) != '\\' ) {
            System.out.print( cout.charAt( i ) );
          } // if
          
          else if ( cout.charAt( i ) == '\\' &&
                    cout.charAt( i + 1 ) == 'n' ) {
            System.out.println();
            i += 1;
          } // else if 
          
        } // for
        

        operate = PeekToken();
      } // while
      
      return postitem;
    } // if
    else {
      while ( operate.mtype.equals( "LS" ) || operate.mtype.equals( "RS" ) ) {
        operate = GetToken();
        postitem = Maybe_additive_exp();
        int retuidint = Integer.parseInt( GetValue( retuid ) );
        int postitemint = Integer.parseInt( GetValue( postitem ) );
        if ( operate.mtype.equals( "LS" ) ) { // <<
          retuidint = retuidint << postitemint;
        } // if 
        else {
          retuidint = retuidint >> postitemint;
        } // else 
        
        if ( retuid.mtype.equals( "CONSTANT" ) ) {
          retuid.mname = Integer.toString( retuidint );
        } // if
        else {
          retuid.mlist.set( retuid.mindex, Integer.toString( retuidint ) );
        } // else
        
        operate = PeekToken();
      } // while 
      
      
    } // else 
     
    return retuid;
  } // Rest_of_maybe_shift_exp()
  
  void Rest_of_maybe_shift_exp_noexe() {
    Rest_of_maybe_additive_exp_noexe();
    MyToken operate = PeekToken(); // << >>
    while ( operate.mtype.equals( "LS" ) || operate.mtype.equals( "RS" ) ) {
      operate = GetToken();
      Maybe_additive_exp_noexe();
      operate = PeekToken();
    } // while 

  } // Rest_of_maybe_shift_exp_noexe()
  
  MyToken Maybe_additive_exp() {
    MyToken retuid = Maybe_mult_exp();
    MyToken operate = PeekToken(); // + -
    // float retuidfloat = 0;
    while ( operate.mtype.equals( "+" ) || operate.mtype.equals( "-" ) ) {
      operate = GetToken();
      MyToken postitem = Maybe_mult_exp();
      
      if ( operate.mtype.equals( "-" ) ) {
        if ( retuid.mrtype.equals( "FLOAT" ) || postitem.mrtype.equals( "FLOAT" ) ) {
          float retuidfloat = Float.parseFloat( GetValue( retuid ) );
          float postitemfloat = Float.parseFloat( GetValue( postitem ) );
          retuidfloat -= postitemfloat;
          if ( retuid.mtype.equals( "CONSTANT" ) ) {
            retuid.mname = Float.toString( retuidfloat );
          } // if
          else {
            retuid.mlist.set( retuid.mindex, Float.toString( retuidfloat ) );
          } // else
          
          retuid.mrtype = "FLOAT";
        } // if 
        else {
          int retuidfloat = Integer.parseInt( GetValue( retuid ) );
          int postitemfloat = Integer.parseInt( GetValue( postitem ) );
          retuidfloat -= postitemfloat;
          if ( retuid.mtype.equals( "CONSTANT" ) ) {
            retuid.mname = Integer.toString( retuidfloat );
          } // if
          else {
            retuid.mlist.set( retuid.mindex, Integer.toString( retuidfloat ) );
          } // else
          
        } // else
      } // if 
      else { // +
        if ( retuid.mrtype.equals( "STRING" ) ) {
          String ans = "";
          String prestr, poststr;
          if ( retuid.mtype.equals( "IDENTIFIER" ) ) {
            prestr = retuid.mlist.get( retuid.mindex );
          } // if
          else {
            prestr = retuid.mname;
          } // else 
            
          if ( postitem.mtype.equals( "IDENTIFIER" ) ) {
            poststr = postitem.mlist.get( postitem.mindex );
          } // if
          else {
            poststr = postitem.mname;
          } // else 
            
          ans = prestr + poststr;
          if ( retuid.mtype.equals( "IDENTIFIER" ) ) {
            retuid.mlist.set( retuid.mindex, ans );
          } // if
          else { // constant
            retuid.mname = ans;
          } // else 
        } // if 
        else { // constent
          if ( retuid.mrtype.equals( "FLOAT" ) || postitem.mrtype.equals( "FLOAT" ) ) {
            float retuidfloat = Float.parseFloat( GetValue( retuid ) );
            float postitemfloat = Float.parseFloat( GetValue( postitem ) );
            retuidfloat += postitemfloat;
            if ( retuid.mtype.equals( "CONSTANT" ) ) {
              retuid.mname = Float.toString( retuidfloat );
            } // if
            else {
              retuid.mlist.set( retuid.mindex, Float.toString( retuidfloat ) );
            } // else
            
            retuid.mrtype = "FLOAT";
          } // if 
          else {
            int retuidfloat = Integer.parseInt( GetValue( retuid ) );
            int postitemfloat = Integer.parseInt( GetValue( postitem ) );
            retuidfloat += postitemfloat;
            if ( retuid.mtype.equals( "CONSTANT" ) ) {
              retuid.mname = Integer.toString( retuidfloat );
            } // if
            else {
              retuid.mlist.set( retuid.mindex, Integer.toString( retuidfloat ) );
            } // else
            
          } // else
         
        } // else
        
      } // else 
      
      operate = PeekToken();
    } // while
    
    return retuid;
  } // Maybe_additive_exp()
  
  void Maybe_additive_exp_noexe() {
    Maybe_mult_exp_noexe();
    MyToken operate = PeekToken(); // + -
    while ( operate.mtype.equals( "+" ) || operate.mtype.equals( "-" ) ) {
      operate = GetToken();
      Maybe_mult_exp_noexe();      
      operate = PeekToken();
    } // while
    
  } // Maybe_additive_exp_noexe()
  
  MyToken Rest_of_maybe_additive_exp( MyToken paid ) {
    MyToken retuid = Rest_of_maybe_mult_exp( paid );
    MyToken operate = PeekToken(); // + -
    // float retuidfloat = 0;
    while ( operate.mtype.equals( "+" ) || operate.mtype.equals( "-" ) ) {
      operate = GetToken();
      MyToken postitem = Maybe_mult_exp();
      
      if ( operate.mtype.equals( "-" ) ) {
        if ( retuid.mrtype.equals( "FLOAT" ) || postitem.mrtype.equals( "FLOAT" ) ) {
          float retuidfloat = Float.parseFloat( GetValue( retuid ) );
          float postitemfloat = Float.parseFloat( GetValue( postitem ) );
          retuidfloat -= postitemfloat;
          if ( retuid.mtype.equals( "CONSTANT" ) ) {
            retuid.mname = Float.toString( retuidfloat );
          } // if
          else {
            retuid.mlist.set( retuid.mindex, Float.toString( retuidfloat ) );
          } // else
          
          retuid.mrtype = "FLOAT";
        } // if 
        else {
          int retuidfloat = Integer.parseInt( GetValue( retuid ) );
          int postitemfloat = Integer.parseInt( GetValue( postitem ) );
          retuidfloat -= postitemfloat;
          if ( retuid.mtype.equals( "CONSTANT" ) ) {
            retuid.mname = Integer.toString( retuidfloat );
          } // if
          else {
            retuid.mlist.set( retuid.mindex, Integer.toString( retuidfloat ) );
          } // else
          
        } // else
      } // if 
      else { // +
        if ( retuid.mrtype.equals( "STRING" ) ) {
          String ans = "";
          String prestr, poststr;
          if ( retuid.mtype.equals( "IDENTIFIER" ) ) {
            prestr = retuid.mlist.get( retuid.mindex );
          } // if
          else {
            prestr = retuid.mname;
          } // else 
            
          if ( postitem.mtype.equals( "IDENTIFIER" ) ) {
            poststr = postitem.mlist.get( postitem.mindex );
          } // if
          else {
            poststr = postitem.mname;
          } // else 
            
          ans = prestr + poststr;
          if ( retuid.mtype.equals( "IDENTIFIER" ) ) {
            retuid.mlist.set( retuid.mindex, ans );
          } // if
          else { // constant
            retuid.mname = ans;
          } // else 
        } // if 
        else { // constent
          if ( retuid.mrtype.equals( "FLOAT" ) || postitem.mrtype.equals( "FLOAT" ) ) {
            float retuidfloat = Float.parseFloat( GetValue( retuid ) );
            float postitemfloat = Float.parseFloat( GetValue( postitem ) );
            retuidfloat += postitemfloat;
            if ( retuid.mtype.equals( "CONSTANT" ) ) {
              retuid.mname = Float.toString( retuidfloat );
            } // if
            else {
              retuid.mlist.set( retuid.mindex, Float.toString( retuidfloat ) );
            } // else
            
            retuid.mrtype = "FLOAT";
          } // if 
          else {
            int retuidfloat = Integer.parseInt( GetValue( retuid ) );
            int postitemfloat = Integer.parseInt( GetValue( postitem ) );
            retuidfloat += postitemfloat;
            if ( retuid.mtype.equals( "CONSTANT" ) ) {
              retuid.mname = Integer.toString( retuidfloat );
            } // if
            else {
              retuid.mlist.set( retuid.mindex, Integer.toString( retuidfloat ) );
            } // else
            
          } // else
         
        } // else
        
      } // else 
      
      operate = PeekToken();
    } // while
    
    return retuid;
  } // Rest_of_maybe_additive_exp()
  
  void Rest_of_maybe_additive_exp_noexe() {
    Rest_of_maybe_mult_exp_noexe();
    MyToken operate = PeekToken(); // + -
    while ( operate.mtype.equals( "+" ) || operate.mtype.equals( "-" ) ) {
      operate = GetToken();
      Maybe_mult_exp_noexe();
      operate = PeekToken();
    } // while
    
  } // Rest_of_maybe_additive_exp_noexe()
  
  MyToken Maybe_mult_exp() {
    MyToken token = Unary_exp();
    MyToken ans = Rest_of_maybe_mult_exp( token );
    
    return ans;
  } // Maybe_mult_exp()
  
  void Maybe_mult_exp_noexe() {
    Unary_exp_noexe();
    Rest_of_maybe_mult_exp_noexe();
  } // Maybe_mult_exp_noexe()
  
  MyToken Rest_of_maybe_mult_exp( MyToken paid ) {
    MyToken operate = PeekToken();
    while ( operate.mtype.equals( "*" ) || operate.mtype.equals( "/" ) || 
            operate.mtype.equals( "%" ) ) {
      operate = GetToken(); // */%
      
      MyToken postitem = Unary_exp();
      if ( operate.mtype.equals( "*" ) ) {
        if ( paid.mrtype.equals( "FLOAT" ) || postitem.mrtype.equals( "FLOAT" ) ) {
          float retuidfloat = Float.parseFloat( GetValue( paid ) );
          float postitemfloat = Float.parseFloat( GetValue( postitem ) );
          retuidfloat *= postitemfloat;
          if ( paid.mtype.equals( "CONSTANT" ) ) {
            paid.mname = Float.toString( retuidfloat );
          } // if
          else {
            paid.mlist.set( paid.mindex, Float.toString( retuidfloat ) );
          } // else
          
          paid.mrtype = "FLOAT";
        } // if 
        else {
          int retuidfloat = Integer.parseInt( GetValue( paid ) );
          int postitemfloat = Integer.parseInt( GetValue( postitem ) );
          retuidfloat *= postitemfloat;
          if ( paid.mtype.equals( "CONSTANT" ) ) {
            paid.mname = Integer.toString( retuidfloat );
          } // if
          else {
            paid.mlist.set( paid.mindex, Integer.toString( retuidfloat ) );
          } // else
          
        } // else 
        
      } // if
      else if ( operate.mtype.equals( "/" ) ) {
        if ( paid.mrtype.equals( "FLOAT" ) || postitem.mrtype.equals( "FLOAT" ) ) {
          float retuidfloat = Float.parseFloat( GetValue( paid ) );
          float postitemfloat = Float.parseFloat( GetValue( postitem ) );
          retuidfloat /= postitemfloat;
          if ( paid.mtype.equals( "CONSTANT" ) ) {
            paid.mname = Float.toString( retuidfloat );
          } // if
          else {
            paid.mlist.set( paid.mindex, Float.toString( retuidfloat ) );
          } // else
          
          paid.mrtype = "FLOAT";
        } // if 
        else {
          int retuidfloat = Integer.parseInt( GetValue( paid ) );
          int postitemfloat = Integer.parseInt( GetValue( postitem ) );
          retuidfloat /= postitemfloat;
          if ( paid.mtype.equals( "CONSTANT" ) ) {
            paid.mname = Integer.toString( retuidfloat );
          } // if
          else {
            paid.mlist.set( paid.mindex, Integer.toString( retuidfloat ) );
          } // else
          
        } // else 
      } // else if
      else {
        int retuidint = Integer.parseInt( GetValue( paid ) );
        int postitemint = Integer.parseInt( GetValue( postitem ) );
        retuidint %= postitemint;
        if ( paid.mtype.equals( "IDENTIFIER" ) ) {
          paid.mlist.set( paid.mindex, Integer.toString( retuidint ) );
        } // if
        else if ( paid.mtype.equals( "CONSTANT" ) ) {
          paid.mname = Integer.toString( retuidint );
        } // else if
        
      } // else 
      
      operate = PeekToken();
    } // while 
    
    return paid;
  } // Rest_of_maybe_mult_exp()
  
  
  void Rest_of_maybe_mult_exp_noexe() {
    MyToken operate = PeekToken();
    while ( operate.mtype.equals( "*" ) || operate.mtype.equals( "/" ) || 
            operate.mtype.equals( "%" ) ) {
      operate = GetToken(); // */%
      Unary_exp_noexe();
      operate = PeekToken();
    } // while 
    
  } // Rest_of_maybe_mult_exp_noexe()
  
  MyToken Unary_exp() {
    MyToken token = PeekToken();
    if ( token.mtype.equals( "+" ) || token.mtype.equals( "-" ) ||
         token.mtype.equals( "!" ) ) {
      int sign = 1; // +: +, -: - 
      while ( token.mtype.equals( "+" ) || token.mtype.equals( "-" ) ||
              token.mtype.equals( "!" ) ) {
        token = GetToken();
        if ( token.mtype.equals( "+" ) ) {
          sign *= 1;
        } // if
        else if ( token.mtype.equals( "-" ) || token.mtype.equals( "!" ) ) {
          sign *= -1;
        } // else if
        
        token = PeekToken();
      } // while         
        
      token = Signed_unary_exp( sign );
    } // if 
    else if ( token.mtype.equals( "PP" ) || token.mtype.equals( "MM" ) ) {
      MyToken pm = GetToken(); // PP || MM
      MyToken id = GetToken(); // id
      token = PeekToken();
      if ( token.mtype.equals( "[" ) ) {
        GetToken(); // [
        token = Expression();
        id.mindex = Integer.parseInt( GetValue( token ) );
        GetToken(); // ]
      } // if
      
      id = Find( id );
      
      if ( pm.mtype.equals( "PP" ) ) {
        float pre = Float.parseFloat( id.mlist.get( id.mindex ) );
        float ans = pre + 1;
        
        if ( id.mrtype.equals( "FLOAT" ) ) {
          id.mlist.set( id.mindex, Float.toString( ans ) );
        } // if
        else {
          int temp = ( int ) ans;
          id.mlist.set( id.mindex, Integer.toString( temp ) );
        } // else 
        
      } // if
      else {
        float pre = Float.parseFloat( id.mlist.get( id.mindex ) );
        float ans = pre - 1;
       
        if ( id.mrtype.equals( "FLOAT" ) ) {
          id.mlist.set( id.mindex, Float.toString( ans ) );
        } // if
        else {
          int temp = ( int ) ans;
          id.mlist.set( id.mindex, Integer.toString( temp ) );
        } // else 
        
      } // else

      token = new MyToken( Find( id ) );
      token.mlist.set( id.mindex, id.mlist.get( id.mindex ) );
      token.mindex = id.mindex;
    } // else if
    else { // unsigned_unary_exp
      token = Unsigned_unary_exp();
    } // else 
    
    return token;
  } // Unary_exp()
  
  void Unary_exp_noexe() {
    MyToken token = PeekToken();
    if ( token.mtype.equals( "+" ) || token.mtype.equals( "-" ) ||
         token.mtype.equals( "!" ) ) {
      while ( token.mtype.equals( "+" ) || token.mtype.equals( "-" ) ||
              token.mtype.equals( "!" ) ) {
        GetToken();
        token = PeekToken();
      } // while         
        
      Signed_unary_exp_noexe();
    } // if 
    else if ( token.mtype.equals( "PP" ) || token.mtype.equals( "MM" ) ) {
      MyToken pm = GetToken(); // PP || MM
      MyToken id = GetToken(); // id
      token = PeekToken();
      if ( token.mtype.equals( "[" ) ) {
        GetToken(); // [
        Expression_noexe();
        GetToken(); // ]
      } // if
      
    } // else if
    else { // unsigned_unary_exp
      Unsigned_unary_exp_noexe();
    } // else 
    
  } // Unary_exp_noexe()
  
  MyToken Signed_unary_exp( int sign ) {
    ArrayList<MyToken> parameter = new ArrayList<MyToken>();
    MyToken token = PeekToken();
    MyToken id = null;
    if ( token.mtype.equals( "IDENTIFIER" ) ) {
      id = GetToken();
      id.mindex = 0;
      token = PeekToken();
      if ( token.mtype.equals( "(" ) ) {
        GetToken(); // (
        parameter = Actual_parameter_list();
        GetToken(); // )
      } // if
      else if ( token.mtype.equals( "[" ) ) {
        GetToken(); // [
        token = Expression();
        id.mindex = Integer.parseInt( GetValue( token ) );
        GetToken(); // ]         
      } // else if 
      
      token = new MyToken( Find( id ) );
      token.mindex = id.mindex;
    } // if 
    else if ( token.mtype.equals( "CONSTANT" ) ) {
      token = GetToken(); // Constant
    } // else if
    else if (  token.mtype.equals( "(" ) ) {
      GetToken(); // (
      token = new MyToken( Expression() );
      GetToken(); // )
    } // else if
    
    if ( sign == -1 ) {
      if ( token.mrtype.equals( "BOOL" ) ) {
        if ( token.mlist.get( token.mindex ).equals( "true" ) ) {
          token.mlist.set( token.mindex, "flase" );
        } // if
        else {
          token.mlist.set( token.mindex, "true" );
        } // else 
        
      } // if 
      else if ( token.mrtype.equals( "TRUE" ) || token.mrtype.equals( "FALSE" ) ) {
        if ( token.mname.equals( "true" ) ) {
          token.mname = "flase";
        } // if
        else {
          token.mname = "true";
        } // else
        
      } // else if
      else if ( token.mtype.equals( "IDENTIFIER" ) || token.mtype.equals( "CONSTANT" ) ) {
        
        if ( token.mtype.equals( "IDENTIFIER" ) && 
             token.mlist.get( token.mindex ).equals( "true" ) ) {
          token.mlist.set( token.mindex, "flase" );
        } // if
        else if ( token.mtype.equals( "IDENTIFIER" ) &&
                  token.mlist.get( token.mindex ).equals( "false" ) ) {
          token.mlist.set( token.mindex, "true" );
        } // else if
        else {          
          if ( token.mrtype.equals( "INT" ) ) {
            int ans = Integer.parseInt( GetValue( token ) );
            ans *= -1;
            if ( token.mtype.equals( "CONSTANT" ) ) {
              token.mname = Integer.toString( ans );
            } // if
            else {
              token.mlist.set( token.mindex, Integer.toString( ans ) );
            } // else 
            
          } // if
          else {
            float ans = Float.parseFloat( GetValue( token ) );
            ans *= -1;
            if ( token.mtype.equals( "CONSTANT" ) ) {
              token.mname = Float.toString( ans );
            } // if
            else {
              token.mlist.set( token.mindex, Float.toString( ans ) );
            } // else 
            
          } // else 
          
        } // else 
        
      } // else if 
      
    } // if    
        
    return token;
  } // Signed_unary_exp()
  
  void Signed_unary_exp_noexe() {
    ArrayList<MyToken> parameter = new ArrayList<MyToken>();
    MyToken token = PeekToken();
    MyToken id = null;
    if ( token.mtype.equals( "IDENTIFIER" ) ) {
      id = GetToken();
      id.mindex = 0;
      token = PeekToken();
      if ( token.mtype.equals( "(" ) ) {
        GetToken(); // (
        parameter = Actual_parameter_list();
        GetToken(); // )
      } // if
      else if ( token.mtype.equals( "[" ) ) {
        GetToken(); // [
        Expression_noexe();
        GetToken(); // ]         
      } // else if 
      
    } // if 
    else if ( token.mtype.equals( "CONSTANT" ) ) {
      GetToken(); // Constant
    } // else if
    else if (  token.mtype.equals( "(" ) ) {
      GetToken(); // (
      Expression_noexe();
      GetToken(); // )
    } // else if
   
  } // Signed_unary_exp_noexe()  
  
  MyToken Unsigned_unary_exp() {
    ArrayList<MyToken> parameter = new ArrayList<MyToken>();
    MyToken token = PeekToken();
    if ( token.mtype.equals( "IDENTIFIER" ) ) {
      MyToken id = GetToken();
      id.mindex = 0;
      token = PeekToken();
      if ( token.mtype.equals( "(" ) ) {
        GetToken(); // (
        parameter = Actual_parameter_list();
        GetToken(); // )
      } // if
      else if ( token.mtype.equals( "[" ) || token.mtype.equals( "PP" ) ||
                token.mtype.equals( "MM" ) ) {
        
        if ( token.mtype.equals( "[" ) ) { // is []
          GetToken(); // [
          token = Expression();
          id.mindex = Integer.parseInt( GetValue( token ) );
          GetToken(); // ]  
          token = PeekToken();
        } // if 
        
        int temp = id.mindex;
        id =  Find( id );
        id.mindex = temp;
        
        if ( token.mtype.equals( "PP" ) ) {
          token = GetToken(); // pp
          if ( id.mrtype.equals( "FLOAT" ) ) {
            float pre = Float.parseFloat( id.mlist.get( id.mindex ) );
            float ans = pre + 1;
            id.mlist.set( id.mindex, Float.toString( ans ) );
            
            token = new MyToken( Find( id ) );
            token.mlist.set( id.mindex, id.mlist.get( id.mindex ) );
            token.mindex = id.mindex;
            
            float nowid = Float.parseFloat( id.mlist.get( id.mindex ) ) - 1;
            token.mlist.set( token.mindex, Float.toString( nowid ) ); // 0519 **************************
          } // if
          else {
            int pre = Integer.parseInt( id.mlist.get( id.mindex ) );
            int ans = pre + 1;
            id.mlist.set( id.mindex, Integer.toString( ans ) );
            
            token = new MyToken( Find( id ) );
            token.mlist.set( id.mindex, id.mlist.get( id.mindex ) );
            token.mindex = id.mindex;
            
            int nowid = Integer.parseInt( id.mlist.get( id.mindex ) ) - 1;
            token.mlist.set( token.mindex, Integer.toString( nowid ) );
          } // else
          
          return token;
        } // if
        else if ( token.mtype.equals( "MM" ) ) {
          token = GetToken(); // MM
          if ( id.mrtype.equals( "FLOAT" ) ) {
            float pre = Float.parseFloat( id.mlist.get( id.mindex ) );
            float ans = pre - 1;
            id.mlist.set( id.mindex, Float.toString( ans ) );
            id.mdot = true;
            token = new MyToken( Find( id ) );
            token.mlist.set( id.mindex, id.mlist.get( id.mindex ) );
            token.mindex = id.mindex;
            
            float nowid = Float.parseFloat( id.mlist.get( id.mindex ) ) + 1;
            token.mlist.set( token.mindex, Float.toString( nowid ) );
            
          } // if
          else {
            int pre = Integer.parseInt( id.mlist.get( id.mindex ) );
            int ans = pre - 1;
            id.mlist.set( id.mindex, Integer.toString( ans ) );
            
            token = new MyToken( Find( id ) );
            token.mlist.set( id.mindex, id.mlist.get( id.mindex ) );
            token.mindex = id.mindex;
            
            int nowid = Integer.parseInt( id.mlist.get( id.mindex ) ) + 1;
            token.mlist.set( token.mindex, Integer.toString( nowid ) );
          } // else
          
          return token;
        } // else if
        
      } // else if 
      
      token = new MyToken( Find( id ) );
      token.mindex = id.mindex;
    } // if 
    else if ( token.mtype.equals( "CONSTANT" ) ) {
      token = GetToken(); // Constant
    } // else if
    else {
      GetToken(); // (
      token = new MyToken( Expression() );
      GetToken(); // )
    } // else 
        
    return token;
  } // Unsigned_unary_exp()
  
  
  void Unsigned_unary_exp_noexe() {
    ArrayList<MyToken> parameter = new ArrayList<MyToken>();
    MyToken token = PeekToken();
    if ( token.mtype.equals( "IDENTIFIER" ) ) {
      GetToken();
      token = PeekToken();
      if ( token.mtype.equals( "(" ) ) {
        GetToken(); // (
        Actual_parameter_list();
        GetToken(); // )
      } // if
      else if ( token.mtype.equals( "[" ) || token.mtype.equals( "PP" ) ||
                token.mtype.equals( "MM" ) ) {
        
        if ( token.mtype.equals( "[" ) ) { // is []
          GetToken(); // [
          Expression_noexe();
          GetToken(); // ]  
          token = PeekToken();
        } // if 
        
        if ( token.mtype.equals( "PP" ) ) {
          token = GetToken(); // pp 
        } // if
        else if ( token.mtype.equals( "MM" ) ) {
          token = GetToken(); // MM
        } // else if
        
      } // else if 
      
    } // if
    else if ( token.mtype.equals( "CONSTANT" ) ) {
      GetToken(); // Constant
    } // else if
    else {
      GetToken(); // (
      Expression_noexe();
      GetToken(); // )
    } // else 
        
  } // Unsigned_unary_exp_noexe()
  
} // class MyExecute


class Main {
  public static void main( String[] args ) throws Exception {
    MyScanner scanner = new MyScanner();
    MyParser parser = new MyParser();
    MyExecute execute = new MyExecute();

    char tcase = scanner.Get();
    while ( tcase != '\n' ) {
      tcase = scanner.Get();
    } // while

    System.out.println( "Our-C running ..." );

    while ( true ) {
      try {
        MyScanner.sline = 1;
        parser.User_input();
        //  System.out.println("===================scorrtoken===========");
        //  for (int i = 0; i < MyParser.scorrtoken.size(); i++ ) {
        //    System.out.print( MyParser.scorrtoken.get( i ).mname + " " );
        //  } // for
        //  System.out.println( "\n===============end scorrtoken===========" );
        
        //  System.out.println("===================stempvar===========");
        //  for (int i = 0; i < MyParser.stempvar.size(); i++ ) {
        //    System.out.println( MyParser.stempvar.get( i ).mname + " " + MyParser.stempvar.get(i).mrtype);
        //  } // for
        //  System.out.println( "\n===============end stempvar===========" );
        
        execute.GetTokenList( MyParser.scorrtoken );
        MyParser.stempvar.clear();
        execute.User_input();
        MyParser.scorrtoken.clear();
        
        
        // System.out.println("===================main stable===========");
        // for (int i = 0; i < MyExecute.stable.size(); i++ ) {
        //   for ( int j = 0; j < MyExecute.stable.get( i ).size(); j++ ) {
        //     System.out.print( MyExecute.stable.get( i ).get( j ).mrtype + " " );
        //     System.out.print( MyExecute.stable.get( i ).get( j ).mname + " " );
        //     System.out.print( MyExecute.stable.get( i ).get( j ).mlength + " " );
        //     for ( int x = 0; x < MyExecute.stable.get( i ).get( j ).mlist.size(); x++ ) {
        //       System.out.print( x + " : " + MyExecute.stable.get( i ).get( j ).mlist.get( x ) +", ");
        //     } // for
            
        //   } // for
        // } // for
        // System.out.println( "\n===========end main stable===============" );  
      
       
      } // try 
      catch ( Lexical_error e ) {
        System.out.println( e.getMessage() );
      } // catch
      catch ( Syntax_error s ) {
        System.out.println( s.getMessage() );
      } // catch 
      catch ( Semantic_error a ) {
        System.out.println( a.getMessage() );
      } // catch 
      catch ( Done d ) {
        System.out.println( d.getMessage() );
        return ;
      } // catch

    } // while 
    
  } // main()
  
} // class Main 