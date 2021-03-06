package org.jetbrains.emacs4ij.jelisp.subroutine;

import org.jetbrains.emacs4ij.jelisp.JelispTestCase;
import org.jetbrains.emacs4ij.jelisp.elisp.LispObject;
import org.jetbrains.emacs4ij.jelisp.elisp.LispSymbol;
import org.junit.Assert;
import org.junit.Test;

public class PredicateTest extends JelispTestCase {
  @Test
  public void testStringp() throws Exception {
    LispObject lispObject = evaluateString("(stringp \"hello\")");
    Assert.assertEquals(LispSymbol.T, lispObject);
    lispObject = evaluateString("(stringp 'hello)");
    Assert.assertEquals(LispSymbol.NIL, lispObject);
  }

  @Test
  public void testSymbolp() throws Exception {
    LispObject lispObject = evaluateString("(symbolp \"hello\")");
    Assert.assertEquals(LispSymbol.NIL, lispObject);
    lispObject = evaluateString("(symbolp 'hello)");
    Assert.assertEquals(LispSymbol.T, lispObject);
  }

  @Test
  public void testIntegerp() throws Exception {
    LispObject lispObject = evaluateString("(integerp 1)");
    Assert.assertEquals(LispSymbol.T, lispObject);
    lispObject = evaluateString("(integerp 'hello)");
    Assert.assertEquals(LispSymbol.NIL, lispObject);
  }

  @Test
  public void testSubrp() throws Exception {
    LispObject lispObject = evaluateString("(subrp 1)");
    Assert.assertEquals(LispSymbol.NIL, lispObject);
    lispObject = evaluateString("(subrp (symbol-function 'if))");
    Assert.assertEquals(LispSymbol.T, lispObject);
    lispObject = evaluateString("(subrp 'if)");
    Assert.assertEquals(LispSymbol.NIL, lispObject);
    lispObject = evaluateString("(subrp (symbol-function 'put))");
    Assert.assertEquals(LispSymbol.T, lispObject);
  }

  @Test
  public void testFunctionp () {
    evaluateString("(defun f () )");
    LispObject lispObject = evaluateString("(functionp 'f)");
    Assert.assertEquals(LispSymbol.T, lispObject);
    lispObject = evaluateString("(functionp (symbol-function 'f))");
    Assert.assertEquals(LispSymbol.T, lispObject);
    lispObject = evaluateString("(functionp (symbol-function 'subrp))");
    Assert.assertEquals(LispSymbol.T, lispObject);
    lispObject = evaluateString("(functionp (symbol-function 'if))");
    Assert.assertEquals(LispSymbol.NIL, lispObject);
    lispObject = evaluateString("(functionp  'subrp)");
    Assert.assertEquals(LispSymbol.T, lispObject);
  }

  @Test
  public void FunctionpLambda() {
    LispObject lispObject = evaluateString("(functionp (lambda () 1))");
    Assert.assertEquals(LispSymbol.T, lispObject);
  }

  @Test
  public void testCommandp () {
    evaluateString("(defun f () (interactive) )");
    LispObject lispObject = evaluateString("(commandp 'f)");
    Assert.assertEquals(LispSymbol.T, lispObject);
    lispObject = evaluateString("(commandp 1)");
    Assert.assertEquals(LispSymbol.NIL, lispObject);
  }

  @Test
  public void testCommandpLambda () {
    LispObject lispObject = evaluateString("(commandp '(lambda () (+ 6 3) (interactive \"f\")))");
    Assert.assertEquals(LispSymbol.T, lispObject);
  }

  @Test
  public void testCommandp_BuiltIn () {
    LispObject lispObject = evaluateString("(commandp 'switch-to-buffer)");
    Assert.assertEquals(LispSymbol.T, lispObject);
    lispObject = evaluateString("(commandp 'if)");
    Assert.assertEquals(LispSymbol.NIL, lispObject);
  }

  @Test
  public void testCommandpKeymap () {
    LispObject lispObject = evaluateString("(commandp 'Control-X-prefix)");
    Assert.assertEquals(LispSymbol.NIL, lispObject);
  }

  @Test
  public void testFboundp () {
    evaluateString("(defun f ())");
    LispObject result = evaluateString("(fboundp 'f)");
    Assert.assertEquals(LispSymbol.T, result);
    result = evaluateString("(fboundp 'if)");
    Assert.assertEquals(LispSymbol.T, result);
    result = evaluateString("(fboundp 'fboundp)");
    Assert.assertEquals(LispSymbol.T, result);
    result = evaluateString("(fboundp 'switch-to-buffer)");
    Assert.assertEquals(LispSymbol.T, result);
  }

  @Test
  public void testDefaultBoundP() {
    LispObject result = evaluateString("(default-boundp 'f)");
    Assert.assertEquals(LispSymbol.NIL, result);
    evaluateString("(setq f 1)");
    result = evaluateString("(default-boundp 'f)");
    Assert.assertEquals(LispSymbol.T, result);
  }

  @Test
  public void testDefaultBoundPDefaultDir() {
    LispObject result = evaluateString("(default-boundp 'default-directory)");
    Assert.assertEquals(LispSymbol.T, result);
    result = evaluateString("(default-boundp 'is-alive)");
    Assert.assertEquals(LispSymbol.NIL, result);
  }

  @Test
  public void testSequenceP() {
    LispObject r = evaluateString("(sequencep ())");
    Assert.assertEquals(LispSymbol.T, r);
    r = evaluateString("(sequencep '())");
    Assert.assertEquals(LispSymbol.T, r);
    r = evaluateString("(sequencep nil)");
    Assert.assertEquals(LispSymbol.T, r);
    r = evaluateString("(sequencep \"hello\")");
    Assert.assertEquals(LispSymbol.T, r);
    r = evaluateString("(sequencep [])");
    Assert.assertEquals(LispSymbol.T, r);
    r = evaluateString("(sequencep '[])");
    Assert.assertEquals(LispSymbol.T, r);
    r = evaluateString("(sequencep '[1 2])");
    Assert.assertEquals(LispSymbol.T, r);
    r = evaluateString("(sequencep '(1 2))");
    Assert.assertEquals(LispSymbol.T, r);

    r = evaluateString("(sequencep 'a)");
    Assert.assertEquals(LispSymbol.NIL, r);
  }

  @Test
  public void testCharacterP() {
    LispObject r = evaluateString("(characterp 5)");
    Assert.assertEquals(LispSymbol.T, r);
    r = evaluateString("(characterp -1)");
    Assert.assertEquals(LispSymbol.NIL, r);
    r = evaluateString("(characterp 4194304)");
    Assert.assertEquals(LispSymbol.NIL, r);
    r = evaluateString("(characterp 4194303)");
    Assert.assertEquals(LispSymbol.T, r);
  }

  @Test
  public void testNotListP() {
    Assert.assertEquals(LispSymbol.NIL, evaluateString("(nlistp nil)"));
    Assert.assertEquals(LispSymbol.NIL, evaluateString("(nlistp '(a . b))"));
    Assert.assertEquals(LispSymbol.T, evaluateString("(nlistp 1)"));
  }

  @Test
  public void zero() {
    Assert.assertEquals(LispSymbol.NIL, evaluateString("(zerop ?q)"));
    Assert.assertEquals(LispSymbol.T, evaluateString("(zerop 0)"));
    Assert.assertEquals(LispSymbol.T, evaluateString("(zerop 0.0)"));
  }
}
