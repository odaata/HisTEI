xquery version "3.0";

(:~
 : Search the Amsterdam corpus and return
 :
 : @author Mike Olson
 : @version 0.1
 :)

import module namespace rpt="http://histei.info/xquery/reports" at "reports.xqm";
import module namespace teix="http://histei.info/xquery/tei" at "tei.xqm";
import module namespace txt="http://histei.info/xquery/tei2text" at "tei2text.xqm";
import module namespace utils="http://histei.info/xquery/utils" at "utils.xqm";

declare namespace file="http://expath.org/ns/file";
declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace tei="http://www.tei-c.org/ns/1.0";

declare variable $userID as xs:string external := "MJO";
declare variable $fileExtension as xs:string external := ".xml";

declare variable $transToGetPath := "/home/mike/Amsterdam/Transcriptions.txt";
declare variable $transPath := "/home/mike/Amsterdam/transcriptions";
declare variable $corpusPath := "/home/mike/Amsterdam/corpus";
declare variable $dbnlPath := "/home/mike/Amsterdam/dbnl/tei";
declare variable $updatedPath := "/home/mike/Amsterdam/updated";
declare variable $tokenizedPath := "/home/mike/Amsterdam/tokenized";

declare variable $contextualInfoPath := "/home/mike/Amsterdam/contextual_info";

declare function local:hits-worden($words as element(tei:w)*) as element(tei:w)* {
    let $wordenUnique := 
        <wordenUnique>
            <rawText>gewoerden</rawText>
            <rawText>gewoorden</rawText>
            <rawText>geworde</rawText>
            <rawText>geworden</rawText>
            <rawText>ghewerden</rawText>
            <rawText>gheworden</rawText>
            <rawText>stoutwierden</rawText>
            <rawText>twaerden</rawText>
            <rawText>twerden</rawText>
            <rawText>verworden</rawText>
            <rawText>warden</rawText>
            <rawText>welgeworden</rawText>
            <rawText>werd'er</rawText>
            <rawText>werdden</rawText>
            <rawText>werde</rawText>
            <rawText>werden</rawText>
            <rawText>werdende</rawText>
            <rawText>werdij</rawText>
            <rawText>werdt</rawText>
            <rawText>werdtdoor</rawText>
            <rawText>wert'er</rawText>
            <rawText>wierd'</rawText>
            <rawText>wierde</rawText>
            <rawText>wierden</rawText>
            <rawText>wierdt</rawText>
            <rawText>woerden</rawText>
            <rawText>woorden</rawText>
            <rawText>worde</rawText>
            <rawText>worden</rawText>
            <rawText>wordende</rawText>
            <rawText>wordt</rawText>
            <rawText>wordtt</rawText>
        </wordenUnique>
    let $wordenTerms := for $term in $wordenUnique/* return string($term)
    return
        $words[txt:attested-forms(.) = $wordenTerms]
};

declare function local:hits($teiDocs as document-node()*, 
                            $conInfoMap as map(xs:string, document-node()), 
                            $rptHeaders as element(teiDoc)*, 
                            $filterFunc as function(element(tei:w)*) as element(tei:w)*) as element(hits) {
    element hits {
        for $rptHeader in $rptHeaders
        let $doc := $teiDocs[tei:TEI/@xml:id eq teix:format-id("TEI", $rptHeader/teiID/text())]
        
        let $words := $doc//tei:w[empty(./(ancestor::tei:note | ancestor::tei:del))]
        let $words := $filterFunc($words)
        for $w at $pos in $words
        let $wordID := string($w/@xml:id)
        let $attForm := txt:attested-forms($w)
        let $rawText := lower-case($attForm)
        order by $rawText, $rptHeader/AMST_GROUPS, $rptHeader/birthYear, $rptHeader/writer, $rptHeader/year, $pos
        return
            element hit {
                $rptHeader/*,
                element position { $pos },
                element attForm { $attForm },
                element rawText { $rawText },
                txt:context($w)/*,
                element wordID { $wordID },
                element wordURI { concat($rptHeader/fullURI/text(), "#", $wordID) }
            }
    }
};

let $teiDocs := teix:collection(utils:saxon-collection-uri($tokenizedPath))
let $conInfoMap := teix:con-info-docs-map($contextualInfoPath)
let $rptHeaders := rpt:headers($teiDocs, $conInfoMap)

let $wordenUnique := doc("file:/home/mike/Downloads/Amsterdam_searches/worden_unique.xml")//rawText[exists(isWorden/text())]/text()

let $ann := <tei:w xml:id="w_sz4_q1l_fr">t<tei:pc>=</tei:pc>est<tei:add><tei:del>w</tei:del>ord</tei:add><tei:del>withdeletions</tei:del> and<tei:pc>=</tei:pc><tei:lb/> spaces</tei:w>
return
    local:hits($teiDocs, $conInfoMap, $rptHeaders, local:hits-worden#1)


(:let $hits := $docs//tei:w[matches(normalize-space(), "^in{1,2}d.{1,2}$", "i")]:)
(:let $hits := $docs//tei:w[matches(normalize-space(), "^m[ie][td]{1,2}.{1,3}$", "i")]:)
(:let $hits := $docs//tei:w[matches(normalize-space(), "w[aeijouw]{1,3}r[td]{1,2}.{1,3}$", "i")]:)
(:let $hits := $docs//tei:w[matches(normalize-space(), "^v[ao]n", "i")] | $docs//tei:w[matches(normalize-space(), "v[ao]n$", "i")]
let $distinct-hits := distinct-values(for $hit in $hits return lower-case(normalize-space($hit)))
return  
    element hits {
        for $hit in $distinct-hits
        order by $hit
        return
            element hit {
                element rawText { $hit },
                element hitCount { count($docs//tei:w[lower-case(normalize-space()) eq $hit]) }
            }
    }:)

(:    doc("/home/mike/Downloads/INL-Tagged_SAA_00231_Marquette_00366_0000000071.xml"):)
(:    doc("/home/mike/Downloads/INL-Tagged_SAA_00231_Marquette_00366_0000000071.xml")//tei:w[contains(string-join(text(), ""), "vande")]:)





