xquery version "3.1";

(:~
 : Generate a tabular report with header values from all TEI documents in a given directory
 :
 : @author Mike Olson
 : @version 0.1
 :
 : Fields
 : - DocID
 : - idNo
 : - Status
 : - StatusDate
 : - StatusUser
 : - Genre
 : - Other catRefs
 : - Date Written
 : - Date Certainty
 : - WriterID
 : - Writer
 : - Location Written
 : - Org as writer
 : - Hand Count
 : - Title
 : - Word Count
 : - Note Count
 : - Filename
 : - Full URI
 :)

import module namespace histei="http://histei.info/xquery/utils/histei" at "utils-histei.xqm";

declare variable $teiURI as xs:anyURI external;
declare variable $contextualInfoURI as xs:anyURI external;

(: Windows URIs:)
(:let $teiURI := xs:anyURI("file:/Z:/home/Amsterdam/tokenized"):)

(: Linux/Mac URIs :)
let $teiURI := xs:anyURI("file:/home/mike/Amsterdam/tokenized")
let $contextualInfoURI := xs:anyURI("file:/home/mike/Amsterdam/contextual_info")

return
element corpusAll { histei:headers-paths($teiURI, $contextualInfoURI) }









