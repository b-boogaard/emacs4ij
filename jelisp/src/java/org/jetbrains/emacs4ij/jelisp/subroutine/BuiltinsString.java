package org.jetbrains.emacs4ij.jelisp.subroutine;

import org.jetbrains.emacs4ij.jelisp.Environment;
import org.jetbrains.emacs4ij.jelisp.elisp.*;
import org.jetbrains.emacs4ij.jelisp.exception.ArgumentOutOfRange;
import org.jetbrains.emacs4ij.jelisp.exception.LispException;
import org.jetbrains.emacs4ij.jelisp.exception.WrongTypeArgumentException;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kate
 * Date: 26/02/12
 * Time: 18:06
 * To change this template use File | Settings | File Templates.
 */
public abstract class BuiltinsString {
    private BuiltinsString () {}

    @Subroutine(value = "format")
    public static LispString format (LispString formatString, @Optional LObject... objects) {
        String s = formatString.getData();
        int k = s.indexOf('%');
        String result = s.substring(0, (k == -1 ? s.length() : k));
        char p = '%';
        int index = 0;
        Character[] formatCharsArray = new Character[] {'s', 'S', 'd', 'o', 'x', 'X', 'e', 'f', 'g', 'c'};
        List<Character> formatChars = Arrays.asList(formatCharsArray);

        while (k != -1) {
            k++;
            while (k < s.length() && s.charAt(k) < 'A' && s.charAt(k) != '%') { //skip flags,width,precision: %<flags><width><precision>character
                k++;
            }
            if (k == s.length())
                throw new LispException("Format string ends in middle of format specifier");
            if (s.charAt(k) == '%') {
                result += '%';
            } else {
                if (index >= objects.length)
                    throw new LispException("Not enough arguments for format string");
                if (!(formatChars.contains(s.charAt(k))))
                    throw new LispException("Invalid format operation %" + s.charAt(k));
                result += objects[index].toString();
                index++;
            }
            /* switch (s.charAt(k)) {
               case 's': //print a string argument.  Actually, prints any object, with `princ'.
                   break;
               case 'S': //print any object as an s-expression (using `prin1').
                   break;
               case '%':
                   break;

               //todo: The argument used for %d, %o, %x, %e, %f, %g or %c must be a number.

               case 'd': //print as number in decimal (%o octal, %x hex)
                   break;
               case 'o': //print as number in octal
                   break;
               case 'x': //print as number in hex
                   break;
               case 'X': //is like %x, but uses upper case.
                   break;
               case 'e': //print a number in exponential notation.
                   break;
               case 'f': //print a number in decimal-point notation.
                   break;
               case 'g': //print a number in exponential notation or decimal-point notation, whichever uses fewer characters.
                   break;
               case 'c': //print a number as a single character.
                   break;


               default:
                   throw new LispException("Invalid format operation %" + s.charAt(k));
           } */
            k++;
            int nextPercent = s.indexOf('%', k);
            result += s.substring(k, (nextPercent == -1 ? s.length() : nextPercent));
            k = nextPercent;
        }
        return new LispString(result);
    }

    @Subroutine(value = "string-match")
    public static LObject stringMatch (Environment environment, LispString regexp, LispString string, @Optional LispInteger start) {
        int from = 0;
        if (start != null) {
            from = start.getData();
            if (from < 0 || from >= string.length())
                throw new ArgumentOutOfRange(string.toString(), start.toString());
        }
        LispSymbol s = environment.find("case-fold-search");
        int r = string.match(regexp, from, (s != null && !s.getValue().equals(LispSymbol.ourNil)));
        if (r == -1)
            return LispSymbol.ourNil;
        return new LispInteger(r);
    }

    @Subroutine("message")
    public static LispString message (LispString formatString, @Optional LObject... args) {
        //todo: write in echo area
        LispString s = format(formatString, args);
        System.out.println(s.getData());
        return s;
    }

    @Subroutine("capitalize")
    public static LObject capitalize (LObject object) {
        if (object instanceof LispString) {
            return ((LispString)object).capitalize();
        }
        if (object instanceof LispInteger) {
            int data = ((LispInteger)object).getData();
            if (data >= 'a' && data <= 'z') {
                return new LispInteger(data - 'a' + 'A');
            }
            return new LispInteger(data);
        }
        throw new WrongTypeArgumentException("char-or-string-p", object.toString());
    }
}