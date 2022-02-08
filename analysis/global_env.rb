require './ir/ast/visitor'
require './lib/env'
require 'set'
require './define'

module Rc
  module Analysis
    class GlobalEnvVisitor
      include AST::Visitor
      attr_reader :define_env, :const_table, :class_table

      def initialize
        @define_env = Env.new
        init_class_table
        @const_table = Set[]
        @fun_env = { }
        @cur_class_name = Rc::Define::GlobalObject
      end

      def cur_class
        @class_table[@cur_class_name]
      end

      def init_class_table
        @class_table = Env.new
        @class_table.define_symbol(Rc::Define::GlobalObject, ClassTable.new)
      end

      def analysis(ast)
        visit(ast)
        GlobalEnv.new(@const_table, @class_table)
      end

      def on_class_define(node)
        # todo:refactor
        # save old
        old_class_name = @cur_class_name
        # make new and update
        @cur_class_name = node.name
        class_table = ClassTable.new
        # define before visit fun, because of this is a context used for visit fun
        @class_table.define_symbol(node.name, class_table)
        # visit and add value to class_table
        # todo:dirty work, will not enter here when you visit Kernel Method
        node.fun_list.each {|f| visit(f)}
        node.var_list.each {|v| class_table.add_instance_var(v.name, v.val)}
        # restore name
        @cur_class_name = old_class_name
      end

      def on_function(node)
        # todo:refactor
        # @define_env.define_symbol(node.name, node) # todo:remove
        @cur_fun_sym = Env.new
        @cur_fun_var_id = 0
        @cur_fun_sym.merge(node.args.map{ |arg| [arg, EnvItemInfo.new(cur_fun_var_id, '')]}.to_h)
        visit(node.stmts)
        @fun_env[node.name] = @cur_fun_sym
        cur_class.add_instance_method(node.name, InstanceMethodInfo.new(node, @cur_fun_sym, node.args))
      end

      def on_string_constant(node)
        @const_table.add node.val
      end

      def on_assign(node)
        super
        unless node.var_obj.is_a? Rc::AST::ClassMemberAccess
          name = node.var_obj.name
          @cur_fun_sym[name] = EnvItemInfo.new(cur_fun_var_id, '')
        end
      end

      def cur_fun_var_id
        @cur_fun_var_id.tap { @cur_fun_var_id += 1 }
      end
    end
  end
end