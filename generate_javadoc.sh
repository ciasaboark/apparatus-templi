javadoc -docletpath TeXDoclet.jar \
	-doclet org.stfm.texdoclet.TeXDoclet \
	-tree \
	-hyperref \
	-output /tmp/out.tex \
	-title "Apparatus Templi" \
	-author "Jonathan Nelson" \
	-sourcepath src/:events/:services/:drivers/:tests/ \
	-subpackages org.apparatus_templi:org.apparatus_templi.event:org.apparatus_templi.xml:org.apparatus_templi.service:org.apparatus_templi.web:org.apparatus_templi.web.generator:org.apparatus_templi.web.handler
