package org.jetbrains.emacs4ij.jelisp.elisp;

import org.jetbrains.emacs4ij.jelisp.Environment;
import org.jetbrains.emacs4ij.jelisp.subroutine.BuiltinsCore;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: kate
 * Date: 2/28/12
 * Time: 3:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class LispSubCharTable extends LispObject implements LispArray {
    private int myDepth; // == one of [1, 2, 3]
    private int myMinChar;
    private LObject[] myContent;

    public LispSubCharTable(int depth, int minChar, LObject init) {
        myDepth = depth;
        myMinChar = (char)minChar;
        myContent = new LObject[getMinVectorSizeToFit() - 1 + CharUtil.charTableSize(depth) - 2];
        Arrays.fill(myContent, init);
    }

    @Override
    public LObject evaluate(Environment environment) {
        return null;
    }

//    returns the length of the shortest vector that would hold this object
    public static int getMinVectorSizeToFit() {
        return 3;
    }

    public void set (int c, LObject value) {
        int i = CharUtil.index(c, myDepth, myMinChar);
        if (myDepth == 3)
            myContent[i] = value;
        else {
            if (!(myContent[i] instanceof LispSubCharTable)) {
                myContent[i] = new LispSubCharTable (myDepth + 1,
                        myMinChar + i * CharUtil.charTableChars(myDepth), myContent[i]);
            }
            ((LispSubCharTable)myContent[i]).set(c, value);
        }
    }

    @Override
    public String toString() {
        String s = "#^^[" + myDepth + ' ' + myMinChar;
        for (LObject item: myContent) {
            s += ' ' + item.toString();
        }
        return s + ']';
    }

    @Override
    public void setItem(int position, LObject value) {
        myContent[position] = value;
    }

    @Override
    public LObject getItem(int position) {
        return myContent[position];
    }

    public static LObject setRange (LObject table, int depth, int minChar, int from, int to, LObject value) {
        int maxChar = minChar + CharUtil.charTableChars(depth) - 1;
        if (depth == 3 || (from <= minChar && to >= maxChar)) {
            return value;
        }
        depth++;
        if (!(table instanceof LispSubCharTable))
            table = new LispSubCharTable(depth, minChar, table);
        if (from < minChar)
            from = minChar;
        if (to > maxChar)
            to = maxChar;
        int i = CharUtil.index(from, depth, minChar);
        int j = CharUtil.index(to, depth, minChar);
        minChar += CharUtil.charTableChars(depth) * i;
        for (; i <= j; i++, minChar += CharUtil.charTableChars(depth)) {
            LispSubCharTable subTable = (LispSubCharTable) table;
            subTable.setItem(i, setRange (subTable.getItem(i), depth, minChar, from, to, value));
        }
        return table;
    }
    
    public LObject ref (int c) {
        LObject value = myContent[CharUtil.index(c, myDepth, myMinChar)];
        if (value instanceof LispSubCharTable)
            value = ((LispSubCharTable) value).ref(c);
        return value;
    }

    public LObject refAndRange (int c, int from, int to, LObject init) {
        int max_char = myMinChar + CharUtil.charTableChars(myDepth - 1) - 1;
        int index = CharUtil.index(c, myDepth, myMinChar);
        LObject value = myContent[index];
        if (value instanceof LispSubCharTable)
            value = ((LispSubCharTable) value).refAndRange(c, from, to, init);
        else if (value.equals(LispSymbol.ourNil))
            value = init;
        int idx = index;
        while (idx > 0 && from < myMinChar + idx * CharUtil.charTableChars(myDepth)) {
            c = myMinChar + idx * CharUtil.charTableChars(myDepth) - 1;
            idx--;
            LObject this_val = myContent[idx];
            if (this_val instanceof LispSubCharTable)
                this_val = ((LispSubCharTable) this_val).refAndRange(c, from, to, init);
            else if (this_val.equals(LispSymbol.ourNil))
                this_val = init;
            if (!BuiltinsCore.eqs(this_val, value)) {
                from = c + 1;
                break;
            }
        }
        while ((c = myMinChar + (index + 1) * CharUtil.charTableChars(myDepth)) <= max_char && to >= c) {
            index++;
            LObject this_val = myContent[index];
            if (this_val instanceof LispSubCharTable)
                this_val = ((LispSubCharTable) this_val).refAndRange(c, from, to, init);
            else if (this_val.equals(LispSymbol.ourNil))
                this_val = init;
            if (!BuiltinsCore.eqs(this_val, value)) {
                to = c - 1;
                break;
            }
        }
        return value;
    }
}
