package parsing.template;

import parsing.antlr.NeuralogicParser;
import parsing.grammarParsing.PlainParseTree;

import java.io.IOException;
import java.io.Reader;
import java.util.logging.Logger;

@Deprecated
public class XmlPlainParseTree extends PlainParseTree {
    private static final Logger LOG = Logger.getLogger(XmlPlainParseTree.class.getName());

    public XmlPlainParseTree(Reader reader) throws IOException {
        super(reader);
    }

    @Override
    public NeuralogicParser.TemplateFileContext getRoot() {
        return null;
    }
}
