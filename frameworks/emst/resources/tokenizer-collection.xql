xquery version "3.0";

(:~
 : Tokenize an entire collection of TEI documents
 :
 : @author Mike Olson
 : @version 0.1
 :)

import module namespace tok="http://histei.info/xquery/tei/tokenizer" at "tokenizer.xqm";

declare variable $userID as xs:string external := "";
declare variable $teiURI as xs:anyURI external;
declare variable $tokenizedURI as xs:anyURI external;

tok:tokenize-collection($teiURI, $tokenizedURI, $userID)


