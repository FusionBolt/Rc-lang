require_relative '../ast/visitor'
require_relative '../../analysis/global_env'
require_relative '../tac/visitor'

module Rc
  module SSA
    def raw_name(n)
      if n.is_a? TAC::Name
        n.name.gsub(/%/, "").split(':')[0]
      elsif n.is_a? String
        n
      else
        raise "invalid tac inst#{n} for raw_name"
      end
    end

    def to_ssa(tac_list)
      visitor = SSATranslator.new
      tac_list = visitor.visit(tac_list)
      name_map = visitor.name_map
      tac_list.map do |inst|
        args = inst.instance_variables.map do |i|
          item = inst.instance_variable_get(i)
          if item.is_a? TAC::Name
            raw_name = raw_name(item)
            if name_map.has_key?(raw_name) && name_map[raw_name] == 0
              item.name = "%#{raw_name}"
            end
            item
          else
            item
          end
        end
        TAC::Quad.new(*args)
      end
    end

    class SSATranslator
      include TAC::Visitor
      attr_reader :name_map

      def initialize
        @name_map = {}
      end

      def ssa_name(name)
        "%#{name}:#{@name_map[name]}"
      end

      def update_map(n)
        if n.is_a? TAC::TempName
          return n.name
        end
        name = SSA.raw_name(n)
        if @name_map.has_key? name
          @name_map[name] += 1
        else
          @name_map[name] = 0
        end
        TAC::Name.new(ssa_name(name))
      end

      def on_quad(inst)
        super
        inst.result = update_map(inst.result)
        inst
      end

      def on_name(inst)
        inst.name = ssa_name(inst.name)
        inst
      end

      def on_temp_name(inst)
        inst.name = "%#{inst.name.gsub(/%/, "")}"
        inst
      end
    end

    # def root_to_ssa(ast)
    #   @env = Analysis::GlobalEnvVisitor.new.analysis(ast)
    # end
    #
    # def fun_to_ssa(ast)
    #   SSAVisitor.new.visit(ast.stmts)
    # end

    # translate a fun body to ssa
    # class SSAVisitor
    #   include Visitor
    #   attr_reader :num_map, :inst_list
    #
    #   def initialize
    #     @num_map = {}
    #     @inst_list = []
    #   end
    #
    #   # todo:hack to hash
    #   def update_map(name)
    #     if @num_map.has_key? name
    #       @num_map[name] += 1
    #     else
    #       @num_map[name] = 0
    #     end
    #     "@#{name}#{@num_map[name]}"
    #   end
    #
    #   def on_stmts(node)
    #     # get multi ssa list
    #     # todo:default return []
    #     node.stmts.reduce([]) {|sum, n| sum + visit(n)}
    #   end
    #
    #   def on_if(node)
    #
    #   end
    #
    #   def on_assign(node)
    #     # todo:first visit expr
    #     unless node.var_obj.is_a? Identifier
    #       raise "not support no id in ssa:on_assign"
    #     end
    #     new_name = visit(node.var_obj)
    #     new_val = visit(node.expr)
    #
    #     # name = node.var_obj.name
    #     # new_name = update_map(name)
    #     # todo:this is not true
    #     [SSA.new(new_name, new_val)]
    #     # a name map to count
    #     # a name map to a usage set
    #   end
    #
    #   def on_new_expr(node)
    #
    #   end
    #
    #   def on_variant(node)
    #   end
    #
    #   def on_identifier(node)
    #     update_map(node.name)
    #   end
    # end

    module_function :to_ssa, :raw_name
  end
end