xquery version "3.0";

(:~
 : Tokenize an entire collection of TEI documents
 :
 : @author Mike Olson
 : @version 0.1
 :)

import module namespace sax="http://histei.info/xquery/utils/saxon" at "utils-saxon.xqm";

declare variable $userID as xs:string external := "";
declare variable $teiURI as xs:anyURI external;
declare variable $tokenizedURI as xs:anyURI external;

sax:tokenize-collection($teiURI, $tokenizedURI, $userID)


