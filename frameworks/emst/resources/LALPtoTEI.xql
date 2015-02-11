xquery version "3.0";
(:~
 : Convert letters in the LALP format to TEI
 : 
 : @author Mike Olson
 : @version 0.1
 : 
 : Information about the LALP format was taken from 
 :   Auer, Anita, Mikko Laitinen, Moragh Gordon & Tony Fairman. 2014. 
 :     An electronic corpus of Letters of Artisans and the Labouring Poor (England, c. 1750-1835): 
 :     compilation principles and coding conventions. In Lieven Vandelanotte, Kristin Davidse, 
 :     Caroline Gentens & Ditte Kimps (eds.), Recent Advances in Corpus Linguistics: Developing and 
 :     Exploiting Corpora, vol. 9, 9–29. (Language and Computers - Studies in Practical Linguistics 78). Amsterdam: Rodopi.
 :
 : LALP Header fields 
 : <F File name>
 : <E Photocopy available: 1/0>
 : <Q Identification number archive>
 : <B Name author>
 : <H Age author>
 : <G Sex author>
 : <R Place author>
 : <N Name applicant>
 : <O Other applicants>
 : <A Age applicant>
 : <S Sex applicant>
 : <P Place applicant>
 : <L Legal parish of settlement>
 : <D Date>
 : <X Number of letters>
 : <W Number of words>
 : <C Topic: Application/Related correspondence>
 : <M Miscellaneous>
 :
 : IrEPL also added a new field
 : <I Addressee>
 :
 : All Fields
 : - Anything containing 'applicant' is ignored (these are filler words for fields not filled in)
 : - Any field that is just an 'X' is ignored (this means, the field is blank)
 : File Names (F)
 : - This field contain errors, so the actual file name will be used instead
 : - This is used as the idno for the TEI text
 : Photocopy available (E)
 : - Only digits will be used in the field - must be 1 or 0 for true/false
 : - This is converted to a note in the header
 : Identification number archive (Q)
 : - The field is separated by forward slashes
 ; - The first part is the archive, the last part is the shelf number 
 :     and everything in between is the collection:
 :   - archive/collection/number
 : - This field is converted to the msDesc in SourceDesc in the TEI Header
 : People Names (B, N, O)
 : - Create a person record for each and link them as addressee in the TEI Header for each letter
 : - Names must be split on semicolons, since the Other applicants (O) field can contain multiple names
 : - Names that are just 'Anonymous' are ignored
 : - Anything containing FRIEND is ignored
 : - Names starting/ending with title(s) should have those saved to a roleName or addName element
 :   - ARCHBISHOP, ARCHBISCHOP, CAPTAIN, EARL, GENERAL, LIEUTENANT, LORD, MANAGER, 
 :      MARQUE, MARQUESS, MARQUIS, MAYOR, MR, MRS, REVEREND, SIR
 : - Titles located next to function words are all included in the roleName element (e.g. Earl of Wharton)
 : - Function Words at the end of a name are converted as a whole phrase to an addName element (e.g. Rory of the Mountain)
 : - If the name starts with the function word, the entire thing is converted to a persName element 
 :     with no further markup of the name (e.g. A trew english freman)
 : - Split names on whitespace
 :   - more than 3 units - just write to name
 :   - 3 units - first, middle, last
 :   - 2 units - first, last
 :   - 1 unit - last
 : - Names are output with camel-cased 
 :   - first letter capitalized for each word
 :   - Prefixes must be mixed case: McDonald, O'Bannion
 :   - function words are all lowercase: of, to, the
 :   - single letters (along with following punct.) are always interpreted as initials (foreName/@full="init")
 :     - Since a single 'a' is more likely an initial, it is always counted as such - as opposed to being a function word (i.e. the article 'a')
 : Addressees (I)
 : - Added by the IrEPL project
 : - Has no overlap with the list of sender and applicant names
 : - Create a person record for each and link them as addressee in the TEI Header for each letter
 : Ages (H, A)
 : - Possible entries are 
 :   - number (25)
 :   - gt/lt followed by a number (<25)
 :   - +/- followed by a number (+25)
 :   - number range (25-30)
 : - Anything containing letters is ignored
 : Sex (G, S)
 : - if the field is equal to F or M or starts with: female or male, it is recorded, otherwise ignored
 : Places (R, P, L)
 : - Place names are complicated and it's unclear what each part of the fields represent
 : - Several errors exist in multiple encodings for the same place as well as *.. used for a name
 : - Therefore, I'm just going to put them in an uncategorized place to be fixed later maybe
 :
 : Notes I made while working through the list of place names
 : - Place names are made up of two levels of representation separated by a forward slash
 :   - The highest level is an abbreviation followed by the full name of lowest level
 :   - The combinations are country/city, county/settlement, city/street
 :   - Letters and forward slashes should be filtered out before processing (field contains errors) 
 : - AH: Armagh
 : - CO: Cork
 : - DL: Donegal
 : - DN: Down
 : - DU: Dublin (city)
 : - ENG: England (country)
 : - FH: Fermanagh
 : - GA: Galway
 : - GY: Galway
 : Dates
 : - Anything with no digits at all is ignored
 : - Separated by forward slashes in the European order (dd/mm/yyyy)
 : - if some aspect of the date is not known, it is represented with XX
 : - each part of the date should be filtered for digits - errors in the data where the slashes are left out
 : - two-digit years are left as they are (producing an error when the TEI doc is validated)
 : - Operators are sometimes used to show uncertainty or ranges: OR, BETWEEN...AND
 : Counts (X, W)
 : - Only digits are accepted that can be converted to integers
 : - this is converted to the extent field in the TEI Header
 : Topic: Application/Related correspondence (C)
 : - Separated by a forward slash
 : - Converted to keywords in textClass in the TEI Header 
 : Miscellaneous (M)
 : - Separated by semicolon
 : - Converted to notes in notesStmt in the TEI Header
:)

import module namespace functx="http://www.functx.com" at "functx.xql";

declare default element namespace "http://www.tei-c.org/ns/1.0";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace file="http://expath.org/ns/file";
declare namespace uuid = "java:java.util.UUID";

declare namespace output = "http://www.w3.org/2010/xslt-xquery-serialization";
declare option output:omit-xml-declaration "no";
declare option output:indent "yes";

(: External variables :)
declare variable $contextualInfoDir as xs:string external := "";
declare variable $userID as xs:string external := "";
declare variable $authority as xs:string external := "";
(: External variables for handling names :)
declare variable $titles as xs:string* external := 
    ("ARCHBISHOP", "ARCHBISCHOP", "CAPTAIN", "COLONEL", "EARL", "GENERAL", "LADY", "LIEUTENANT", "LORD", "MANAGER", 
        "MARQUE", "MARQUESS", "MARQUIS", "MAYOR", "MR", "MRS", "MS", "REVEREND", "SIR");
declare variable $functionWords as xs:string* external := ("of", "to", "the");
declare variable $namePrefixes as xs:string* external := ("Mc", "O'", "O");

declare variable $titlesRE as xs:string? := 
    let $re := string-join(for $item in local:get-titles() return concat("(", $item, ")"), "|")
    return 
        if ($re ne "") then concat("^(", $re, ")\.?$") else ();
declare variable $functionWordsRE as xs:string? := 
    let $re := string-join(for $item in local:get-function-words() return concat("(", $item, ")"), "|")
    return 
        if ($re ne "") then concat("^(", $re, ")$") else ();
declare variable $namePrefixesRE as xs:string? := 
    let $re := string-join(for $item in $namePrefixes return concat("(", $item, ")"), "|")
    return 
        if ($re ne "") then concat("^(", $re, ")") else ();

declare function local:parse-values($values as xs:string*) as xs:string* {
    for $val in $values
    for $split in tokenize($val, ";")
    return
        normalize-space($split)
};

declare function local:parse-names($names as xs:string*) as element(name)* {
    let $names := local:parse-values($names)
    let $names := for $name in $names return lower-case($name)
    (: name fields sometimes have 'and' as separator, decided also to get commas in case they occur at some point :)
    let $names := for $name in $names for $split in tokenize($name, ",| and ") return $split
    for $name in $names
    (: Filter out names that don't represent actual people :)
    where not($name = ("?", "anonymous") or contains($name, "friend"))
    return
        element name { local:unclear($name) }
};

declare function local:get-titles() as xs:string* {
    for $title in $titles
    return
        lower-case(normalize-space($title))
};

declare function local:isTitle($token) as xs:boolean {
    if (exists($titlesRE)) then
        matches(xs:string($token), $titlesRE, "i")
    else
        false()
};

declare function local:get-function-words() as xs:string* {
    for $word in $functionWords
    return
        lower-case(normalize-space($word))
};

declare function local:isFunctionWord($token) as xs:boolean {
    if (exists($functionWordsRE)) then
        matches(xs:string($token), $functionWordsRE, "i")
    else
        false()
};

declare function local:person($headers as element(headers)*) as document-node() {
    let $names := distinct-values($headers/B/text() | $headers/N/text() | $headers/O/text())
    let $names := 
        for $name in $names
        for $splitName in tokenize($name, ";")
        return
            normalize-space($splitName)
    let $nameMap := map:new(
        for $name in $names
        return
            map:entry($name, $headers[./*[local-name() = ("B", "N", "O") and text() eq $name]])
    )
    return
    document {
        <?xml-stylesheet type="text/css" href="http://emergingstandards.eu/css/contextual_info.css"?>,
        <TEI xml:lang="en" xmlns="http://www.tei-c.org/ns/1.0">
            <teiHeader>
                <fileDesc>
                    <titleStmt>
                        <title>List of People for {$authority}</title>
                    </titleStmt>
                    <publicationStmt>
                        <authority>{$authority}</authority>
                    </publicationStmt>
                    <sourceDesc>
                        <p>Born Digital.</p>
                    </sourceDesc>
                </fileDesc>
            </teiHeader>
            <text>
                <body>
                    <listPerson>
                        <listPerson>
                            <person xml:id="person_ksf_xvv_zq">
                                <persName>
                                    <roleName/>
                                    <forename/>
                                    <nameLink/>
                                    <surname/>
                                    <surname/>
                                </persName>
                                <sex/>
                                <education/>
                                <occupation/>
                                <faith/>
                                <socecStatus/>
                                <birth>
                                    <settlement/>
                                </birth>
                                <death>
                                    <settlement/>
                                </death>
                                <residence>
                                    <settlement/>
                                </residence>
                                <affiliation>
                                    <orgName/>
                                </affiliation>
                                <event type="marriage">
                                    <label>Marriage</label>
                                    <note type="marriage_partner">
                                        <persName/>
                                    </note>
                                </event>
                            </person>
                        </listPerson>
                        <listPerson type="editor">
                            <person xml:id="person_lsf_xvv_zq">
                                <persName>
                                    <forename/><surname/>
                                </persName>
                            </person>
                        </listPerson>
                        <listRelation type="personal">
                            <relation name="parent"/>
                            <relation name="spouse"/>
                        </listRelation>
                    </listPerson>
                </body>
            </text>
        </TEI>
    }
};

declare function local:teiHeader($headers as element(headers)) as element(teiHeader) {
    <teiHeader>
        <fileDesc>
            <titleStmt>
                <title>Levensverhaal van Maria de Neufville, 'Verhaal van mijn droevig leven', en
                   stichtelijke overpeinzingen. Zonder datum.</title>
                <respStmt>
                   <resp key="trc">Transcriber</resp>
                   <name>Jamie Nelemans</name>
                </respStmt>
                <respStmt>
                   <resp key="crr">Corrector</resp>
                   <name>Mike Olson</name>
                </respStmt>
             </titleStmt>
            <publicationStmt>
                <authority>{$authority}</authority>
                <idno>{ $headers/F/text()[1] }</idno>
            </publicationStmt>
            <notesStmt>
                <note type="lindeman">
                   <lb break="yes"/>LindemanID: 183<lb/>Type: Egodocumenten<lb/>EntryNumber:
                   183<lb/>Author_Name: Maria de Neufville.<lb/>Author_BirthDeath: Amsterdam, 4 februari
                   1699 - Mijdrecht, 19 december 1779.<lb/>Author_Bio: Dochter van Isaak de Neufville
                   (1638-1710), koopman, en Maria Grijspeert (-1726). Doopsgezind.<lb/>Doc_Location: GA
                   Amsterdam, FA Brants 1179.<lb/>Doc_Format: 21 x 33; 28 p.<lb/>Doc_Title: Verhaal van
                   mijn droevig leeven.<lb/>Doc_Language: <lb/>Doc_Type: Autobiografie.<lb/>Doc_Goal:
                   <lb/>Doc_Date: 1710 - 1770.<lb/>Doc_Contents: Aantekeningen van de levensloop van de
                   auteur, teleurstellingen en tegenslagen; godsdienstige
                   overpeinzingen.<lb/>Doc_Editions: <lb/>Author_Bio_Dict: I.H.van Eeghen, 'Herengracht
                   475, het huis van juffrouw [Petronella] de Neufville', /Maandblad Amstelodamum/
                   LIX(1972) 73-77.<lb/>Comments: <lb/>
                </note>
             </notesStmt>
            <sourceDesc>
                <msDesc>
                   <msIdentifier>
                      <repository ref="org:SAA"/>
                      <collection ref="org:collection_11" type="collection">Familie Brants en
                         aanverwante families</collection>
                      <collection ref="org:inventory_38" type="inventory">Levensverhaal van Maria de
                         Neufville, 'Verhaal van mijn droevig leven', en stichtelijke overpeinzingen.
                         Zonder datum.</collection>
                      <idno>1179</idno>
                   </msIdentifier>
                </msDesc>
            </sourceDesc>
        </fileDesc>
        <profileDesc>
             <creation>
                <date cert="medium" from="1710" to="1770" type="written"/>
                <persName ref="psn:person_58" type="author"/>
                <placeName ref="plc:place_lvx_lpl_2n"/>
                <date type="sent"/>
                <date type="received"/>
                <persName type="sender"/>
                <persName type="addressee"/>
                <settlement type="sent"/>
                <settlement type="received"/>
             </creation>
             <textClass>
                <catRef target="gen:CORRESPONDENCE"/>
                <keywords>
                 <term type="genre">non-fictie</term>
                 <term type="subgenre">non-fictie/biografie, non-fictie/koloniën-reizen</term>
                </keywords>
             </textClass>
             <handNotes>
                <handNote scope="major" scribeRef="psn:person_58" xml:id="Maria"/>
             </handNotes>
          </profileDesc>
        <revisionDesc>
            <change status="converted" when="{current-dateTime()}" who="psn:person_{$userID}">Converted from the LALP format by HisTEI</change>
        </revisionDesc>
    </teiHeader>
};

declare function local:get-lalp-letters($inputDir as xs:string) as element(letters) {
    element letters {
        let $fileExtension := ".txt"
        for $file in file:list($inputDir)[ends-with(., $fileExtension)]
        let $basename := substring-before($file, $fileExtension)
        order by $file
        return
            let $transPath := concat($inputDir, file:dir-separator(), $file)
            let $lines := file:read-text-lines($transPath)
            let $headerRE := "^\s*<(.*?)>?\s*$"
            let $headerLocs := for $line at $n in $lines return if (matches($line, $headerRE)) then $n else ()
            let $headers := 
                for $loc in $headerLocs 
                let $headerText := replace($lines[$loc], $headerRE, "$1")
                let $key := substring($headerText, 1, 1)
                (: Use actual file basename instead of Filename field - contains errors and isn't unique :)
                let $value := if ($key eq "F") then $basename else substring($headerText, 3)
                (: Filter out empty fields (="X") and erroneous empty fields (contains("applicant")) :)
                let $value := 
                    if (upper-case($value) eq "X" or contains($value, "applicant")) then
                        ()
                    else
                        $value
                return 
                    element { $key } { $value }
                    
            let $body := subsequence($lines, $headerLocs[last()] + 1)
            return
                element letter { 
                    attribute filename { $file },
                    element headers {
                        attribute total { count($headers) },
                        $headers
                    },
                    element body { string-join($body, "&#10;") }
                }
    }
};

(:declare function local:unclear-token($token as xs:string?) as element(token)? {
    let $matches := analyze-string($token, "\{+(.*)\}+|\{+(.*)|(.*)\}+")
    for $element in $matches/*
    let $elementName := local-name($element)
    let $newToken := element token {
        if ($elementName eq "match") then
            let $value := $element/fn:group/text()[1]
            return
                if ($value eq "") then
                    ()
                else
                    element unclear { $value } 
        else
            $element/text()
    }
    return
        if (xs:string($newToken) eq "") then
            ()
        else
            $newToken
};:)

declare function local:unclear($content as xs:string?) {
    let $tokens := tokenize($content, "\s+")
    let $unclearTokenLocs := 
        for tumbling window $window in $tokens
        start $first at $firstLoc when contains($first, "{")
        end $last at $lastLoc when contains($last, "}")
        return
            for $n in ($firstLoc to $lastLoc) return $n

    let $totalTokens := count($tokens)
    let $newContent := 
        element content {
            for $token at $n in $tokens
            let $newToken := 
                if ($n = $unclearTokenLocs) then
                    let $matches := analyze-string($token, "\{+(.*)\}+|\{+(.*)|(.*)\}+")/*
                    let $totalMatches := count($matches)
                    for $element in $matches
                    let $elementName := local-name($element)
                    let $value := if ($elementName eq "match") then $element/fn:group/text()[1] else $element/text()[1]
                    return
                        if (empty($value)) then
                            ()
                        else if ($totalMatches eq 1 or $elementName eq "match") then
                            element unclear { $value }
                        else
                            text { $value }
                else
                    text { $token }
            return 
               ( $newToken, if ($n lt $totalTokens and exists($newToken)) then " " else () )
        }
    return
        $newContent/node()
};

declare function local:get-prefixed-name($name as xs:string?) as xs:string? {
    if ($name eq "" or empty($namePrefixesRE)) then
        ()
    else
        let $matches := analyze-string($name, $namePrefixesRE, "i")
        let $firstElement := $matches/*[1]
        return
            if (local-name($firstElement) eq "match") then
                let $matchGroup := xs:integer($firstElement/fn:group/fn:group/@nr)
                let $prefix := $namePrefixes[$matchGroup - 1]
                let $nonMatch := $matches/*[2]/text()
                return
                    concat($prefix, functx:capitalize-first($nonMatch))
            else
                ()
};

declare function local:camel-case-names($names as xs:string*, $isFirst as xs:boolean?) as xs:string* {
    let $isFirst := if (empty($isFirst)) then false() else $isFirst
    for $name at $n in $names
    let $prefixedName := local:get-prefixed-name($name)
    return
        if (exists($prefixedName)) then
            $prefixedName
        else if ($isFirst and $n eq 1) then
            functx:capitalize-first($name)
        else if (local:isFunctionWord($name)) then
            lower-case($name)
        else
            functx:capitalize-first($name)
};

declare function local:camel-case-names($names as xs:string*) as xs:string? {
    local:camel-case-names($names, ())
};

declare function local:roleName($content, $elementType as xs:string?) as element() {
    let $elementType := if (starts-with($elementType, "add")) then "addName" else "roleName"
    return
        element { $elementType } { $content }
};

declare function local:roleName($content) as element() {
    local:roleName($content, ())
};

declare function local:isInitial($name as xs:string?) as xs:boolean {
    matches($name, "^(\p{L}\.?)+$")
};

declare function local:forename($content, $type as xs:string?) as element(forename) {
    element forename {
        if (exists($type)) then attribute type { $type } else (),
        if (local:isInitial(xs:string(element token { $content }))) then attribute full { "init" } else (),
        $content
    }
};

declare function local:forename($content) as element(forename) {
    local:forename($content, ())
};

declare function local:surname($content) as element(surname) {
    element surname { $content }
};

declare function local:person-names($names as xs:string*) {
    let $total := count($names)
    return
        if ($total gt 3) then
            string-join(local:camel-case-names($names, true()), " ")
        else
        (
            local:forename(if ($total gt 1) then $names[1] else ()), " ",
            local:forename(if ($total gt 2) then $names[2] else (), "middle"), " ",
            local:surname($names[last()])
        )
};

(: Paths :)
let $projectDir := "/home/mike/Downloads/EMST_output"
let $contextualInfoDir := concat($projectDir, file:dir-separator(), "contextual_info")
let $lalpInputDir := concat($projectDir, file:dir-separator(), "utf8")
let $teiOutputDir := concat($projectDir, file:dir-separator(), "tei")

let $letters := local:get-lalp-letters($lalpInputDir)
let $headers := $letters/letter/headers
(:let $allNameFields := ($headers/B, $headers/N, $headers/O, $headers/I)

let $allNamesElements := local:parse-names($allNameFields/text())
let $allNames := for $nameElement in $allNamesElements return xs:string($nameElement)
(\: Get all the unique names :\)
let $uniqueNames := distinct-values($allNames):)
return
    element names {
        let $field := "Mr. {a} { henry H. goulburn} Ma{yor of Lon}don and william {a hay}"
        let $tokens := tokenize($field, "\s+")
        let $unclearTokenLocs := 
        for tumbling window $window in $tokens
        start $first at $firstLoc when contains($first, "{")
        end $last at $lastLoc when contains($last, "}")
        return
            for $n in ($firstLoc to $lastLoc) return $n

        (:let $newToken := 
            let $matches := analyze-string("{", "\{+(.*)\}+|\{+(.*)|(.*)\}+")/*
            let $totalMatches := count($matches)
            for $element in $matches
            let $elementName := local-name($element)
            let $value := if ($elementName eq "match") then $element/fn:group/text()[1] else $element/text()[1]
            return
                if (empty($value)) then
                    ()
                else if ($totalMatches eq 1 or $elementName eq "match") then
                    element unclear { $value }
                else
                    $value:)
        (:let $field := "{henry goulburn}"
        let $field := "william {a hay}":)
        return
(:            for $token in $tokens return element token { $token }:)
(:            analyze-string("{", "\{+(.*)\}+|\{+(.*)|(.*)\}+")/*:)
(:            local:unclear($field):)
(:            empty($newToken):)
            local:parse-names($field)
    }
    (:let $origTokens := tokenize($field, "\s+")
    let $unclearTokenLocs := 
        for tumbling window $window in $origTokens
        start $first at $firstLoc when contains($first, "{")
        end $last at $lastLoc when contains($last, "}")
        return
            for $n in ($firstLoc to $lastLoc) return $n
    (\: Remove brackets from the words before processing them 
        - original tokens are used to markup those areas as <unclear> :\)
    let $tokens := for $token in $origTokens return replace($token, "[\{\}]+", "")
    let $tokens := local:camel-case-names($tokens)
    return
        element names {
            for $token at $n in $tokens
            return
                if ($token eq "") then
                    ()
                else if ($n = $unclearTokenLocs) then
                    let $matches := analyze-string($token, "\{+(.*)\}+|\{+(.*)|(.*)\}+")
                    for $element in $matches/*
                    let $elementName := local-name($element)
                    let $newToken := element token {
                        if ($elementName eq "match") then
                            let $value := $element/fn:group/text()[1]
                            return
                                if ($value eq "") then
                                    ()
                                else
                                    element unclear { $value } 
                        else
                            $element/text()
                    }
                    return
                        if (xs:string($newToken) eq "") then
                            ()
                        else
                            $newToken
                else
                    element token { $token }
        }:)
    (:element people {
        for $name in $uniqueNames
        let $headerFields := $allNameFields[local-name() = ("B", "N", "O", "I") and $name = local:parse-names(text())]
        let $totalFields := count($headerFields)
        order by $name
        return
            element person { 
                element persName { 
                    let $tokens := tokenize($name, "\s+")
                    let $unclearTokenLocs := 
                        for tumbling window $window in $tokens
                        start $first at $firstLoc when contains($first, "{")
                        end $last at $lastLoc when contains($last, "}")
                        return
                            for $n in ($firstLoc to $lastLoc) return $n
                    (\: Remove brackets from the words before processing them 
                        - original tokens are used to markup those areas as <unclear> :\)
                    let $tokens := for $token in $tokens return replace($token, "[\{\}]+", "")
                    let $tokens := 
                        for $token at $n in $tokens
                        return
                            element token {
                                if ($n = $unclearTokenLocs) then
                                    local:unclear($token)
                                else
                                    $token
                            }
                    
                    let $nonNameLocs := 
                        for $token at $n in $tokens 
                        let $token := xs:string($token)
                        return 
                            if (local:isTitle($token) or local:isFunctionWord($token)) then 
                                $n 
                            else 
                                ()
                    
                    let $nonNameParts := 
                        if (exists($nonNameLocs)) then
                            if ($nonNameLocs[1] eq 1) then
                                subsequence($tokens, 1, $nonNameLocs[last()]) 
                            else 
                                subsequence($tokens, $nonNameLocs[1])
                        else
                            ()
                    let $nameParts := 
                        if (exists($nonNameLocs)) then 
                            if ($nonNameLocs[1] eq 1) then
                                subsequence($tokens, $nonNameLocs[last()] + 1) 
                            else 
                                subsequence($tokens, 1, $nonNameLocs[1] - 1)
                        else
                            $tokens    
                    return
                        if (empty($nonNameLocs) or $nonNameLocs[1] eq 1) then
                            ( local:roleName($nonNameParts, true()), " ", local:person-names($nameParts) )
                        else
                            ( local:person-names($nameParts), " ", local:roleName($nonNameParts) )
                },
                <note type="importedName">{$name}</note>
            }
    }:)
        
        
(:,
element allHeaders { 
attribute total { $totalFields },
for $field in $headerFields
let $header := $field/parent::*
return
utils:replace-content($header, $header/node(), attribute type { local-name($field) })
}:)

(:let $names := $letters//R/text() | $letters//P/text():)
(:let $names := distinct-values($letters//B/text() | $letters//N/text() | $letters//O/text())
let $names := 
    for $name in $names
    for $splitName in tokenize($name, ";")
    return
        normalize-space($splitName)
let $addressees := $letters//I/text()
let $addressees := 
    for $name in $addressees
    for $splitName in tokenize($name, ";")
    return
        normalize-space($splitName)
let $uniqueNames := distinct-values($names)
let $uniqueAddressees := distinct-values($addressees)
return
    element addresseesJoinPeople {
        for $name in $uniqueNames[. = $uniqueAddressees]
        order by $name
        return
            element name { $name }
            (\:if (count($letters//F[text() eq $F]) gt 1) then
                element letter {
                    $letter/@filename,
                    element F { $F }
                }
            else
                ():\)
    }:)

    
    (:element lines {
        for $line in $lines
        return
            element line { $line }
    }:)

(:for tumbling window $w in $lines
        start $first when matches($first, "^<.*>$")
        return
            element window { $w }:)
            
            
            
