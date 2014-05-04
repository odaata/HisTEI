<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet exclude-result-prefixes="xs xd tei" version="3.0"
                xmlns:tei="http://www.tei-c.org/ns/1.0" xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xd:doc scope="stylesheet">
        <xd:desc>
            <xd:p>
                <xd:b>Created on:</xd:b>
                Feb 10, 2014
            </xd:p>
            <xd:p>
                <xd:b>Author:</xd:b>
                Mike Olson
            </xd:p>
            <xd:p>Return all Genres found in the resources/genre.xml file as CCFilter items</xd:p>
        </xd:desc>
    </xd:doc>

    <xsl:template name="start">
        <xsl:variable name="categories"
                      select="doc('genre.xml')//tei:classDecl/tei:taxonomy[1]/tei:category"/>

        <items action="replace">
            <xsl:apply-templates select="$categories">
                <xsl:sort select="@xml:id"/>
            </xsl:apply-templates>
        </items>
    </xsl:template>

    <xsl:template match="tei:category">
        <item annotation="{normalize-space(./tei:catDesc/text())}" value="#{@xml:id}"/>
    </xsl:template>

</xsl:stylesheet>
