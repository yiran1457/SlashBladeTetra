package net.yiran.sbtetra.api;

import net.yiran.expressionlib.expr.Expression;
import net.yiran.expressionlib.expr.ExpressionBuilder;
import net.yiran.sbtetra.Config;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Expressions {
    public static ExpressionCache bladeAttack = new ExpressionCache(new String[]{"refine", "isFiercerEdge", "baseDamage"}, Config.Server.REFINE_ATTACK_RULE);
    public static ExpressionCache soulBladeMapping = new ExpressionCache(new String[]{"refine"}, Config.Server.SOUL_BLADE_MAPPING_RULE);

    public static class ExpressionCache {
        public String lastStringExpression;
        public Supplier<String> stringExpressionGetter;
        public Expression lastExpression;
        public Consumer<ExpressionBuilder> handler;
        public String[] variables;

        public ExpressionCache(String[] variables, Supplier<String> stringExpressionGetter) {
            this(variables, stringExpressionGetter, null);
        }

        public ExpressionCache(String[] variables, Supplier<String> stringExpressionGetter, Consumer<ExpressionBuilder> handler) {
            this.variables = variables;
            this.stringExpressionGetter = stringExpressionGetter;
            this.handler = handler;
        }

        public Expression getExpression() {
            String expression = stringExpressionGetter.get();
            if (expression.equals(lastStringExpression)) {
                return lastExpression;
            }
            ExpressionBuilder builder = new ExpressionBuilder(expression).variables(this.variables);
            if (handler != null) {
                handler.accept(builder);
            }
            Expression compiled = builder.build();
            this.lastStringExpression = expression;
            return this.lastExpression = compiled;
        }
    }
}
