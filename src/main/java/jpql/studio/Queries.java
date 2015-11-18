package jpql.studio;

/**
 *
 * @author Victor
 */
public class Queries {

    private static final char SEPARATOR = ';';

    public static String getQuery(String text, int position) {
        int start = text.lastIndexOf(SEPARATOR, position - 1);
        if (start == -1) {
            start = 0;
        } else {
            start++;
        }
        int end = text.indexOf(SEPARATOR, position);
        if (end == -1) {
            end = text.length();
        }
        return text.substring(start, end);
    }

    public static String getQueryFromJavaCode(String code) {
        StringBuilder builder = new StringBuilder();
        char[] chars = code.toCharArray();
        int i = 0;
        StringBuilder tmp = null;
        while (i < chars.length) {
            char current = chars[i];
            char next = i < chars.length -1 ? chars[i + 1] : 0;
            i++;
            if (current == '\\' && next == 'n') {
                current = '\n';
                i++;
            }
            if (current == '\\' && next == '"') {
                current = '"';
                i++;
            } else if (current == '"') {
                if (tmp == null) {
                    tmp = new StringBuilder();
                } else {
                    builder.append(tmp);
                    tmp = null;
                }
                continue;
            }
            if (tmp != null) {
                tmp.append(current);
            }
        }
        return builder.length() == 0 ? code: builder.toString();
    }
    
    public static String getJavaCodeFromQuery(String query) {
        String[] tokens = query.split("\n");
        StringBuilder builder=new StringBuilder();
        for(int i=0; i <tokens.length; i++) {
            builder.append('"').append(tokens[i]);
            if(i < tokens.length-1) {
                builder.append("\\n");
            }
            builder.append('"');
            if(i < tokens.length-1) {
                builder.append("+\n");
            }
        }
        return builder.toString().trim();
    }

}
