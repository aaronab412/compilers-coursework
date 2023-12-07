import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
// newer versiuon  //visitinvoke  newline space and skip and reapeat;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class SimpleLangInterpreter extends AbstractParseTreeVisitor<Integer> implements SimpleLangVisitor<Integer> {

    private final Map<String, SimpleLangParser.DecContext> global_funcs = new HashMap<>();
    private final Stack<Map<String, Integer>> frames = new Stack<>();

    public Integer visitProgram(SimpleLangParser.ProgContext ctx, String[] args)
    {

        for (int i = 0; i < ctx.dec().size(); ++i) {

            SimpleLangParser.DecContext dec = ctx.dec(i);
            SimpleLangParser.Typed_idfrContext typedIdfr = dec.typed_idfr();
            global_funcs.put(typedIdfr.Idfr().getText(), dec);


        }

        SimpleLangParser.DecContext main = global_funcs.get("main");

        Map<String, Integer> newFrame = new HashMap<>();

        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("true")) {
                newFrame.put(main.vardec().typed_idfr(i).Idfr().getText(), 1);
            } else if (args[i].equals("false")) {
                newFrame.put(main.vardec().typed_idfr(i).Idfr().getText(), 0);
            } else {
                newFrame.put(main.vardec().typed_idfr(i).Idfr().getText(), Integer.parseInt(args[i]));
            }
        }

        frames.push(newFrame);
        return visit(main);

    }
    @Override
    public Integer visitProg(SimpleLangParser.ProgContext ctx) {
        throw new RuntimeException("Should not be here!");
    }

    //done
    @Override
    public Integer visitDec(SimpleLangParser.DecContext ctx) {
        Integer returnValue = visit(ctx.body());
        frames.pop();
        return returnValue;
    }

    @Override
    public Integer visitVardec(SimpleLangParser.VardecContext ctx) {
        return null;
    }// this can stay blank

    @Override
    public Integer visitTyped_idfr(SimpleLangParser.Typed_idfrContext ctx) {
        return null;// this can stay blank


    }

    @Override
    public Integer visitType(SimpleLangParser.TypeContext ctx) {
        throw new RuntimeException("Should not be here!");
    }

    @Override
    public Integer visitBody(SimpleLangParser.BodyContext ctx) {
        Integer returnValue = null;
        for (int i = 0; i < ctx.typed_assign().size(); i++) {
            SimpleLangParser.Typed_assignContext assign = ctx.typed_assign(i);
            visit(assign);
        }
        for (int i = 0; i < ctx.ene().exp().size(); ++i) {
            SimpleLangParser.ExpContext exp = ctx.ene().exp(i);
            returnValue = visit(exp);
        }
        return returnValue;
    }



    @Override
    public Integer visitEne(SimpleLangParser.EneContext ctx) {
        Integer returnValue = null;
        for (int i = 0; i < ctx.exp().size(); ++i) {
            SimpleLangParser.ExpContext exp = ctx.exp(i);
            returnValue = visit(exp);
        }
        return returnValue;
    }// needs to be done

    @Override
    public Integer visitBlock(SimpleLangParser.BlockContext ctx) {

         return visit(ctx.ene());  // double check this

    }

    @Override
    public Integer visitTyped_assign(SimpleLangParser.Typed_assignContext ctx) {
        frames.peek().put(ctx.typed_idfr().Idfr().getText(), visit(ctx.exp()));
        return null;
    }

    @Override
    public Integer visitAssignExpr(SimpleLangParser.AssignExprContext ctx) {
        SimpleLangParser.ExpContext rhs = ctx.exp();
        frames.peek().put(ctx.Idfr().getText(), visit(rhs));
        return null;
    }

    @Override
    public Integer visitBinOpExpr(SimpleLangParser.BinOpExprContext ctx) {
        SimpleLangParser.ExpContext operand1 = ctx.exp(0);
        Integer oprnd1 = visit(operand1);
        SimpleLangParser.ExpContext operand2 = ctx.exp(1);
        Integer oprnd2 = visit(operand2);//oprnd 2 has value here



        switch (((TerminalNode) (ctx.binop().getChild(0))).getSymbol().getType()) {

            case SimpleLangParser.Eq ->  {

                return ((Objects.equals(oprnd1, oprnd2)) ? 1 : 0);

            }
            case SimpleLangParser.Less -> {

                return ((oprnd1 < oprnd2) ? 1 : 0);

            }
            case SimpleLangParser.Great-> {

                return ((oprnd1 > oprnd2) ? 1 : 0);

            }
            case SimpleLangParser.LessEq -> {

                return ((oprnd1 <= oprnd2) ? 1 : 0);

            }
            case SimpleLangParser.GreatEq -> {

                return ((oprnd1 >= oprnd2) ? 1 : 0);

            }
            case SimpleLangParser.Plus -> {

                return oprnd1 + oprnd2;

            }
            case SimpleLangParser.Minus -> {

                return oprnd1 - oprnd2;

            }
            case SimpleLangParser.Times -> {

                return oprnd1 * oprnd2;

            }
            case SimpleLangParser.Divide -> {

                return Math.round(oprnd1 / oprnd2);

            }
            case SimpleLangParser.And -> {

                return (oprnd1 & oprnd2) ;//need to figure out how to do this cheeck the first 2 to figure this out

            }
            case SimpleLangParser.Or -> {

                return oprnd1 | oprnd2;

            }
            case SimpleLangParser.Xor -> {
                return (oprnd1^oprnd2);

            }
            default -> {
                throw new RuntimeException("Shouldn't be here - wrong binary operator.");
            }

        }

    }



    @Override public Integer visitInvokeExpr(SimpleLangParser.InvokeExprContext ctx)
    {

        SimpleLangParser.DecContext dec = global_funcs.get(ctx.Idfr().getText());
        Map<String, Integer> newFrame = new HashMap<>();
        for (int i = 0; i < dec.vardec().typed_idfr().size(); i++) {
        newFrame.put(dec.vardec().typed_idfr(i).Idfr().getText(), visit(ctx.args().exp(i)));}
        frames.push(newFrame);
        return visit(dec);

    }

    @Override
    public Integer visitBlockExpr(SimpleLangParser.BlockExprContext ctx) {
        return visit(ctx.block());
    }

    @Override
    public Integer visitIfExpr(SimpleLangParser.IfExprContext ctx) {

        SimpleLangParser.ExpContext cond = ctx.exp();
        Integer condValue = visit(cond);
        if (condValue > 0) {

            SimpleLangParser.BlockContext thenBlock = ctx.block(0);
            return visit(thenBlock);

        } else {

            SimpleLangParser.BlockContext elseBlock = ctx.block(1);
            return visit(elseBlock);

        }
    }

    @Override
    public Integer visitWhileExpr(SimpleLangParser.WhileExprContext ctx) {
        SimpleLangParser.ExpContext cond = ctx.exp();
        Integer condValue = visit(cond);
        while(condValue >= 1){
            SimpleLangParser.BlockContext whileblock = ctx.block();//look at this
             visit(whileblock);
             condValue = visit(cond);
        }
        return null;
    }

    @Override
    public Integer visitRepeat(SimpleLangParser.RepeatContext ctx) {// need to make sure that i have thee
        Integer returnValue = null;
        SimpleLangParser.ExpContext cond = ctx.exp();
        Integer condValue = null;

        do {
            returnValue = visit(ctx.block());

             condValue = visit(ctx.exp());
            // Repeat until the condition becomes true
        } while (condValue <= 0);

        return returnValue;
    }


    @Override
    public Integer visitPrintExpr(SimpleLangParser.PrintExprContext ctx) {
        SimpleLangParser.ExpContext exp = ctx.exp();

        if (((TerminalNode) exp.getChild(0)).getSymbol().getType() == SimpleLangParser.Space) {

            System.out.print(" ");

        } else if (((TerminalNode) exp.getChild(0)).getSymbol().getType() == SimpleLangParser.NewLine) {

            System.out.println();

        } else {

            System.out.print(visit(exp));

        }

        return null;
    }

    @Override
    public Integer visitSpaceExpr(SimpleLangParser.SpaceExprContext ctx) {
        return 0;// done ?
    }

    @Override
    public Integer visitSkip(SimpleLangParser.SkipContext ctx) {
        return 0;// done ?
    }

    @Override
    public Integer visitNewLine(SimpleLangParser.NewLineContext ctx) {
        return 0;
    }


    @Override
    public Integer visitIdExpr(SimpleLangParser.IdExprContext ctx) {
        return frames.peek().get(ctx.Idfr().getText());
    }

    @Override
    public Integer visitIntExpr(SimpleLangParser.IntExprContext ctx) {
        return Integer.parseInt(ctx.IntLit().getText());
    }

    @Override
    public Integer visitBoolLit(SimpleLangParser.BoolLitContext ctx) {
        if(ctx.BoolLit().getText().equals("true"))
        {return 1;}
         return 0 ;
    }

    @Override
    public Integer visitArgs(SimpleLangParser.ArgsContext ctx) {
        return null;// should be fine blank

        // SimpleLangParser.ArgsContext param = dec.typed_idfr();

        //      for (int i = 0; i < ctx.args().exp().size(); i++) {
        //        SimpleLangParser.ExpContext exp = ctx.args().exp(i);// make sure i change this to go through all the expressions and not just one of them
        //    newFrame.put(param.Idfr().getText(), visit(exp));// check if it works with only one parameter
        //  }

    }

    @Override
    public Integer visitEqBinop(SimpleLangParser.EqBinopContext ctx) {
        throw new RuntimeException("Should not be here!");
    }

    @Override
    public Integer visitLessBinop(SimpleLangParser.LessBinopContext ctx) {
        throw new RuntimeException("Should not be here!");
    }

    @Override
    public Integer visitLessEqBinop(SimpleLangParser.LessEqBinopContext ctx) {
        throw new RuntimeException("Should not be here!");
    }

    @Override
    public Integer visitGreatBinop(SimpleLangParser.GreatBinopContext ctx) {
        throw new RuntimeException("Should not be here!");
    }

    @Override
    public Integer visitGreatEqBinop(SimpleLangParser.GreatEqBinopContext ctx) {
        throw new RuntimeException("Should not be here!");
    }

    @Override
    public Integer visitPlusBinop(SimpleLangParser.PlusBinopContext ctx) {
        throw new RuntimeException("Should not be here!");
    }

    @Override
    public Integer visitMinusBinop(SimpleLangParser.MinusBinopContext ctx) {
        throw new RuntimeException("Should not be here!");
    }

    @Override
    public Integer visitTimesBinop(SimpleLangParser.TimesBinopContext ctx) {
        throw new RuntimeException("Should not be here!");
    }

    @Override
    public Integer visitDivideBinop(SimpleLangParser.DivideBinopContext ctx) {
        throw new RuntimeException("Should not be here!");
    }

    @Override
    public Integer visitAndBinop(SimpleLangParser.AndBinopContext ctx) {
        throw new RuntimeException("Should not be here!");
    }

    @Override
    public Integer visitOrBinop(SimpleLangParser.OrBinopContext ctx) {
        throw new RuntimeException("Should not be here!");
    }

    @Override
    public Integer visitXorBinop(SimpleLangParser.XorBinopContext ctx) {
        throw new RuntimeException("Should not be here!");
    }


}