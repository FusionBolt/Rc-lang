require_relative '../ast/visitor'
require 'set'

module Rc
  module Analysis
    class CallGraph
      attr_reader :call_map

      def initialize(fun_table)
        @call_map = Hash.new
        @fun_table = fun_table
      end

      def to_s
        @call_map.to_s
      end

      # caller -> multi callee
      def analysis(fun)
        callee_name_list = [fun.name]
        begin
          result = find_all(callee_name_list)
          @call_map.merge!(result)
          callee_name_list = result.values.inject(Set[], &:merge).filter { |n| !(@call_map.keys.include? n) }
        end while callee_name_list.size != 0
        self
      end

      # name[] -> { name -> callee set }
      def find_all(callee_name_list)
        visitor = CallGraphVisitor.new
        callee_name_list.map do |fun_name|
          f = @fun_table[fun_name]
          f_callee = visitor.analysis(f)
          [f.name, f_callee]
        end.to_h
      end

      # reverse key and map
      def find_usage(fun)
        Helper.reverse_kv(@call_map)[fun].to_a
      end

      class CallGraphVisitor
        include Visitor
        attr_reader :callee

        def analysis(fun)
          @callee = Set[]
          visit(fun.stmts)
          @callee
        end

        # todo:a error, should get
        def on_if(node) = node.instance_variables.each(&:visit)

        def on_fun_call(node) = @callee.add(node.name)
      end
    end
  end
end