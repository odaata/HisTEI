xquery version "3.0";

(:~
 : Tokenize an entire collection of TEI documents
 :
 : @author Mike Olson
 : @version 0.1
 :)

import module namespace histei="http://histei.info/xquery/utils/histei" at "utils-histei.xqm";

declare variable $userID as xs:string external := "";
declare variable $teiURI as xs:anyURI external;
declare variable $tokenizedURI as xs:anyURI external;

histei:tokenize-collection($teiURI, $tokenizedURI, $userID)


