require_relative '../ast/visitor'
require_relative '../env'

module Rc
  module Analysis
    class GlobalEnvVisitor
      include Visitor
      attr_reader :env

      def initialize
        @env = Env.new
      end

      def analysis(ast)
        visit(ast)
        @env
      end

      def on_class_define(node)
        @env.define_symbol(node.name, node)
      end

      def on_function(node)
        @env.define_symbol(node.name, node)
      end
    end
  end
end