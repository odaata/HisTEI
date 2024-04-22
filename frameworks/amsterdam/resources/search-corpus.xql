xquery version "3.0";

(:~
 : Search the Amsterdam corpus and return
 :
 : @author Mike Olson
 : @version 0.1
 :)

import module namespace sax="http://histei.info/xquery/utils/saxon" at "utils-saxon.xqm";
import module namespace teix="http://histei.info/xquery/tei" at "tei.xqm";
import module namespace txt="http://histei.info/xquery/tei2text" at "tei2text.xqm";

declare namespace file="http://expath.org/ns/file";
declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace tei="http://www.tei-c.org/ns/1.0";

declare variable $userID as xs:string external := "MJO";
declare variable $fileExtension as xs:string external := ".xml";

declare variable $inlLemmaURL := "http://gtb.inl.nl/iWDB/search?actie=article&amp;wdb=WNT&amp;id=";

declare variable $transToGetPath := "/home/mike/Amsterdam/Transcriptions.txt";
declare variable $transPath := "/home/mike/Amsterdam/transcriptions";
declare variable $corpusPath := "/home/mike/Amsterdam/corpus";
declare variable $dbnlPath := "/home/mike/Amsterdam/dbnl/tei";
declare variable $updatedPath := "/home/mike/Amsterdam/updated";
declare variable $tokenizedPath := "/home/mike/Amsterdam/tokenized";
declare variable $taggedPath := "/home/mike/Amsterdam/inl_tagged";

declare variable $contextualInfoPath := "/home/mike/Amsterdam/contextual_info";

declare variable $amsterdamHeaders := doc("/home/mike/Amsterdam/analysis/query_results/headers-Amsterdam.xml")/*/*;

declare function local:div-types($teiDocs as document-node()*) as element(divTypes) {
    element divTypes {
        for $div in $teiDocs//tei:div
        group by $divType := lower-case($div/@type)
        order by $divType
        return
            element divType { 
                element type { $divType },
                element total { count($div) }
            }
    }
};

(:
 : Filter out only words within valid divTypes (where @type is nothing or one of entry, letter, notes)
 :  and only words not within a note or a deleted segment
 : - Div selection was based on analysis of all divTypes available in the Amsterdam corpus (see divTypes-Amsterdam.xml)
:)
declare function local:filter-ling-data($words as element(tei:w)*) as element(tei:w)* {
    $words[ancestor::tei:div[string(@type) = ("","entry","letter","notes")] and not((ancestor::tei:note | ancestor::tei:del | ancestor::tei:w))]
};

declare function local:ling-data($docs) as element(tei:w)* {
    $docs//tei:body/tei:div[string(@type) = ("","entry","letter","notes")]//tei:w[not((ancestor::tei:note | ancestor::tei:del | ancestor::tei:w))]
};

declare function local:filter-worden($words as element(tei:w)*) as element(tei:w)* {
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

declare function local:hits-worden($teiDocs as document-node()*, 
                            $rptHeaders as element(teiDoc)*,
                            $taggedDocs as document-node()*) as element(hits) {
    
    local:hits($teiDocs, $rptHeaders, $taggedDocs, local:filter-worden#1, local:filter-ling-data#1)
};

declare function local:hits-ue($teiDocs as document-node()*, 
                            $rptHeaders as element(teiDoc)*) as element(hits) {
    
    let $filterFunc := function($words as element(tei:w)*) as element(tei:w)* {
        $words[exists(tei:abbr) and matches(txt:attested-forms(.), "^[uvw]", "i")]
    }
    return
        local:hits($teiDocs, $rptHeaders, (), $filterFunc, ())
};

declare function local:hits($teiDocs as document-node()*, 
                            $rptHeaders as element(teiDoc)*, 
                            $taggedDocs as document-node()*,
                            $filterFunc as function(element(tei:w)*) as element(tei:w)*,
                            $preFilterFunc as function(element(tei:w)*) as element(tei:w)*?) as element(hits) {
    element hits {
        for $rptHeader in $rptHeaders
        let $doc := $teiDocs[tei:TEI/@xml:id eq teix:format-id("TEI", $rptHeader/teiID/text())]
        
        let $words := $doc//tei:w
        let $words := if (exists($preFilterFunc)) then $preFilterFunc($words) else $words
        let $totalWordsSearched := count($words)
        
        let $words := $filterFunc($words)
        for $w at $pos in $words
        let $wordID := string($w/@xml:id)
        let $attForm := txt:attested-forms($w)
        let $rawText := lower-case($attForm)
        order by $rawText, $rptHeader/AMST_GROUPS, $rptHeader/birthYear, $rptHeader/writer, $rptHeader/year, $pos
        return
            element hit {
                $rptHeader/*,
                element totalSearched { $totalWordsSearched },
                element position { $pos },
                element attForm { $attForm },
                element rawText { $rawText },
                if (exists($taggedDocs)) then local:append-inl-words($taggedDocs, $wordID) else (),
                txt:context($w)/*,
                element wordID { $wordID },
                element wordURI { concat($rptHeader/fullURI/text(), "#", $wordID) }
            }
    }
};

declare function local:append-inl-words($taggedDocs as document-node()*, $wordID as xs:string) as element()* {
    let $isNewWordFunc := function($ann as element()) as xs:boolean { starts-with($ann/@xml:id, "pc.") }
    
    let $taggedWord := $taggedDocs//tei:w[@xml:id eq $wordID]
    let $newPrevWords := 
        $taggedWord/preceding-sibling::tei:w[$isNewWordFunc(.)] intersect $taggedWord/preceding-sibling::*[not($isNewWordFunc(.))][1]/following-sibling::*
        
    let $newPostWords := 
        $taggedWord/following-sibling::tei:w[$isNewWordFunc(.)] intersect $taggedWord/following-sibling::*[not($isNewWordFunc(.))][1]/preceding-sibling::*
    
    for $word at $n in ( $newPrevWords, $taggedWord, $newPostWords )
    let $inlLemmaID := normalize-space(($word/tei:interpGrp/tei:interp[@type eq "lemmaId"])[1])
    let $inlLemmaID := if ($inlLemmaID = ("", "nil")) then () else $inlLemmaID
    return (
        element { concat("inlLemma", $n) } { string($word/@lemma) },
        element { concat("inlPOS", $n) } { string($word/@type) },
        element { concat("inlAttForm", $n) } { 
            if (exists($word/tei:w)) then 
                string(($word/tei:w/text())[1]) 
            else 
                string($word/text()[1]) 
        },
        element { concat("inlLemmaID", $n) } { $inlLemmaID }
    )
};

declare function local:inl-concordance($taggedDocs as document-node()*) as element(inlConcordance) {
    element inlConcordance {
        element posTags {
            for $pos in $taggedDocs//tei:w/@type
            group by $groupBy := string($pos)
            order by lower-case($groupBy)
            return
                element posTag { element pos { $groupBy }, element total { count($pos) } }
        },
        element lemmaTags {
            for $lemma in $taggedDocs//tei:w/@lemma
            group by $groupBy := string($lemma)
            order by lower-case($groupBy)
            return
                element lemmaTag { element lemma { $groupBy }, element total { count($lemma) } }
        }
    }
};

let $ann := <tei:w xml:id="w_sz4_q1l_fr">t<tei:pc>=</tei:pc>est<tei:add><tei:del>w</tei:del>ord</tei:add><tei:del>withdeletions</tei:del> and<tei:pc>=</tei:pc><tei:lb/> spaces</tei:w>

(:let $corpus := teix:collection(sax:collection-uri($corpusPath)):)

let $tokenizedDocs := teix:collection(sax:collection-uri($tokenizedPath))
(:let $conInfoMap := teix:con-info-docs-map(sax:collection-uri($contextualInfoPath)):)
(:let $rptHeaders := rpt:headers($tokenizedDocs, $conInfoMap):)
let $taggedDocs := teix:collection(sax:collection-uri($taggedPath))

let $wordenHits := local:hits-worden($tokenizedDocs, $amsterdamHeaders, $taggedDocs)
return
    $wordenHits
    (:element inlVerbLemmas {
        let $words := local:ling-data($taggedDocs)[starts-with(@type, "VRB")]
(\:        let $words := $words[not(ancestor::tei:w)]:\)
        
        for $word in $words
        group by $inlPOS := string($word/@type), $inlLemma := string($word/@lemma)
        order by $inlPOS, $inlLemma
        return
            element verb {
                element pos { $inlPOS },
                element lemma { $inlLemma },
                element total { count($word) }
            }
    }:)
    
    
    (:element hits {
        for $rawText in distinct-values($ueHits/hit/rawText)
        order by $rawText
        return
            element rawText { $rawText }
    }:)


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





