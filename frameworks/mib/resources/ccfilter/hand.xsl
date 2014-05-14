<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet exclude-result-prefixes="xs xd" version="2.0"
                xmlns:tei="http://www.tei-c.org/ns/1.0" xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
                xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xd:doc scope="stylesheet">
        <xd:desc>
            <xd:p>
                <xd:b>Created on:</xd:b>
                Feb 16, 2014
            </xd:p>
            <xd:p>
                <xd:b>Author:</xd:b>
                Mike Olson
            </xd:p>
            <xd:p>Returns all the handNotes in the document as combobox list items.</xd:p>
        </xd:desc>
    </xd:doc>

    <xd:doc>
        <xd:desc>This predefined parameter is the system Id of the edited document for which the
            content completion is invoked. You can load it using the doc() function.
        </xd:desc>
    </xd:doc>
    <xsl:param name="documentSystemID"/>

    <xsl:template name="start">
        <!-- Get all the handNote elements from the current system ID. -->
        <xsl:variable name="handNotes"
                      select="doc('file:/home/mike/Dropbox/EmergingStandards/TEI%20practice%20sessions/Test%20letter%20set/Hutchinson-1646-28-10.xml')//tei:handNote"/>
        <items action="replace">
            <xsl:apply-templates select="$handNotes">
                <xsl:sort select="@xml:id"/>
            </xsl:apply-templates>
        </items>
    </xsl:template>

    <xsl:template match="tei:handNote">
        <item annotation="{normalize-space(string())}" value="#{@xml:id}"/>
    </xsl:template>

</xsl:stylesheet>
