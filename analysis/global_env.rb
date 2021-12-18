require_relative '../ast/visitor'
require_relative '../env'
require 'set'

module Rc
  module Analysis
    class GlobalEnvVisitor
      include Visitor
      attr_reader :env, :sym_table

      def initialize
        @env = Env.new
        @sym_table = Set[]
      end

      def analysis(ast)
        visit(ast)
        [@env, @sym_table]
      end

      def on_class_define(node)
        @env.define_symbol(node.name, node)
      end

      def on_function(node)
        @env.define_symbol(node.name, node)
        visit(node.stmts)
      end

      def on_string_constant(node)
        @sym_table.add node.val
      end
    end
  end
end