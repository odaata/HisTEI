xquery version "3.0";

(:~
 : Build the Amsterdam corpus based on values converted to tab delimited text from the Transcriptions.xlm Excel worksheet
 :
 : @author Mike Olson
 : @version 0.1
 :)

import module namespace utils="http://histei.info/xquery/utils" at "utils.xqm";

declare namespace file="http://expath.org/ns/file";
declare namespace tei="http://www.tei-c.org/ns/1.0";

declare namespace output="http://www.w3.org/2010/xslt-xquery-serialization";
declare option output:omit-xml-declaration "no";
declare option output:indent "yes";

declare variable $userID as xs:string external := "";
declare variable $transURI as xs:anyURI external;
declare variable $tokenizedURI as xs:anyURI external;

let $transPath := "/home/mike/Amsterdam/Transcriptions.txt"
let $transToGet := utils:parse-tab-file($transPath)
return
    element transcriptions {
        $transToGet 
    }