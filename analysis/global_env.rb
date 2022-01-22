require './ir/ast/visitor'
require './lib/env'
require 'set'

module Rc
  module Analysis
    class GlobalEnvVisitor
      include AST::Visitor
      attr_reader :define_env, :const_table, :class_table

      def initialize
        @define_env = Env.new
        @class_table = Env.new
        @const_table = Set[]
        @fun_env = { }
      end

      def analysis(ast)
        visit(ast)
        GlobalEnv.new(@define_env, @const_table, @fun_env)
      end

      def on_class_define(node)
        class_table = ClassTable.new
        node.fun_list.each {|f| class_table.add_instance_method(f.name, visit(f))}
        node.var_list.each {|v| class_table.add_instance_var(v.name, v.val)}
        @class_table.define_symbol(node.name, class_table)
      end

      def on_function(node)
        @define_env.define_symbol(node.name, node)
        # todo:refactor
        @cur_fun_sym = Env.new
        @cur_fun_var_id = 0
        @cur_fun_sym.merge(node.args.map{ |arg| [arg, EnvItemInfo.new(cur_fun_var_id, '')]}.to_h)
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