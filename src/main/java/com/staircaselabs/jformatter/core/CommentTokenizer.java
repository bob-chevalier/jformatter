package com.staircaselabs.jformatter.core;

//import com.google.googlejavaformat.java.JavacTokens.AccessibleReader;
//import com.sun.tools.javac.parser.JavadocTokenizer;
//import com.sun.tools.javac.parser.JavadocTokenizer.JavadocComment;
import com.sun.tools.javac.parser.JavaTokenizer;
import com.sun.tools.javac.parser.ScannerFactory;

import com.sun.tools.javac.parser.Tokens.Comment;
import com.sun.tools.javac.parser.Tokens.Comment.CommentStyle;
import com.sun.tools.javac.parser.UnicodeReader;
//import com.sun.tools.javac.parser.JavaTokenizer.BasicComment;
import com.sun.tools.javac.util.*;

import java.nio.*;

//import static com.google.common.base.Preconditions.checkArgument;
import static com.sun.tools.javac.util.LayoutCharacters.*;

/** An extension to the base lexical analyzer that captures
 *  and processes the contents of doc comments.  It does so by
 *  translating Unicode escape sequences and by stripping the
 *  leading whitespace and starts from each line of the comment.
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class CommentTokenizer extends JavaTokenizer {

    /** Create a scanner from the input array.  The array must have at
     *  least a single character of extra space.
     */
    public CommentTokenizer(ScannerFactory fac, char[] input, int inputLength) {
        super(fac, input, inputLength);
    }

//    @Override
//    protected void processLineTerminator( int pos, int endPos ) {
//        System.out.println("processTerminator(" + pos
//                           + "," + endPos + ")=|" +
//                           new String(reader.getRawCharacters(pos, endPos))
//                           + "|");
//    }

    @Override
    protected Comment processComment(int pos, int endPos, CommentStyle style) {
        char[] buf = reader.getRawCharacters(pos, endPos);
//        return new SimpleComment(new DocReader(fac, buf, buf.length, pos), style);
        return new SimpleComment( buf, pos, style );
    }

    static class SimpleComment implements Comment {

        private final int startPos;
//        private final int endPos;
//        private final int commentLength;
//        private final AccessibleReader reader;
        
        private final CommentStyle style;
        private String text = null;
        private final char[] buffer;

        public SimpleComment( char[] buffer, int startPos, CommentStyle style ) {
//            int startPos, int endPos, AccessibleReader reader, CommentStyle style) {
          this.startPos = startPos;
//          this.endPos = endPos;
//          this.reader = reader;
          this.style = style;
//          this.commentLength = endPos - startPos;
            this.buffer = buffer;
        }

        /**
         * Returns the source position of the character at index {@code index} in the comment text.
         *
         * <p>The handling of javadoc comments in javac has more logic to skip over leading whitespace
         * and '*' characters when indexing into doc comments, but we don't need any of that.
         */
        @Override
        public int getSourcePos( int idx ) {
            if( idx < 0 || idx > buffer.length - 1 ) {
                //TODO don't throw runtimeexception?
                throw new RuntimeException( "BFC expected " + idx + " in range [0, " + (buffer.length - 1) );
            } else {
                return startPos + idx;
            }
        }

        @Override
        public CommentStyle getStyle() {
          return style;
        }

        @Override
        public String getText() {
          if (text == null) {
            text = new String( buffer );
          }
          return text;
        }

        /**
         * We don't care about {@code @deprecated} javadoc tags (see the DepAnn check).
         *
         * @return false
         */
        @Override
        public boolean isDeprecated() {
          return false;
        }

        @Override
        public String toString() {
          return String.format("Comment: '%s'", getText());
        }
      }

}
