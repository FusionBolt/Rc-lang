require './ir/ast/visitor'
require './lib/env'
require 'set'

module Rc
  module Analysis
    class GlobalEnvVisitor
      include AST::Visitor
      attr_reader :define_env, :const_table

      def initialize
        @define_env = Env.new
        @const_table = Set[]
        @fun_env = { }
      end

      def analysis(ast)
        visit(ast)
        GlobalEnv.new(@define_env, @const_table, @fun_env)
      end

      def on_class_define(node)
        @define_env.define_symbol(node.name, node)
      end

      def on_function(node)
        @define_env.define_symbol(node.name, node)
        @cur_fun_sym = Env.new
        @cur_fun_var_id = 0
        visit(node.stmts)
        @fun_env[node.name] = @cur_fun_sym
      end

      def on_string_constant(node)
        @const_table.add node.val
      end

      def on_assign(node)
        # todo:when member access, this error
        super
        name = node.var_obj.name
        @cur_fun_sym[name] = EnvItemInfo.new(cur_fun_var_id, '')
      end

      def cur_fun_var_id
        @cur_fun_var_id.tap { @cur_fun_var_id += 1 }
      end
    end
  end
end