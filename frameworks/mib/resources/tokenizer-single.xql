xquery version "3.0";

(:~
 : Tokenize a single TEI document from within an Oxygen framework
 :
 : @author Mike Olson
 : @version 0.1
 :)

import module namespace tok="http://histei.info/xquery/tei/tokenizer" at "tokenizer.xqm";

declare default element namespace "http://www.tei-c.org/ns/1.0";

declare namespace output="http://www.w3.org/2010/xslt-xquery-serialization";
declare option output:omit-xml-declaration "no";
declare option output:indent "no";

declare variable $userID as xs:string external := "";

tok:tokenize(/TEI, $userID)




