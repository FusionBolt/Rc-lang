require './ir/ast/visitor'
require './lib/env'

module Rc::TAC
  # todo:mixin code which not visitor
  class TacTranslator
    include Rc::AST::Visitor
    attr_reader :tac_list

    def initialize
      local_init
      @const_table = Rc::ConstTable.new
      @sym_table = {}
    end

    def translate(ast, env)
      def_list = visit(ast)
      TACRoot.new(def_list, env, {}, @const_table)
    end

    def local_init
      @tmp_count = 0
      @tac_list = []
      @label_count = 0
    end

    def get_tmp_name
      TempName.new("#{@tmp_count}").tap{ @tmp_count += 1 }
    end

    def generate_label
      Label.new("L#{@label_count}").tap{ @label_count += 1 }
    end

    # def on_stmts(node)
    #   node.stmts.reduce([]) { |sum, n| sum + [visit(n)] }
    # end

    def on_assign(node)
      name = visit(node.var_obj)
      expr = visit(node.expr)
      if expr.is_a? Quad
        @tac_list[-1].result = name
      else
        inst = Quad.new(EmptyOp.new, name, expr, EmptyValue.new)
        @tac_list.push inst
      end
      # translate last tac
    end

    def get_result(ret_val)
      if ret_val.is_a? Quad
        ret_val.result
      else
        ret_val
      end
    end

    def on_binary(node)
      first_tac = visit(node.lhs)
      second_tac = visit(node.rhs)
      inst = Quad.new(node.op.op, get_tmp_name, get_result(first_tac), get_result(second_tac))
      @tac_list.push inst
      inst
    end

    def on_if(node)
      after_if = Label.new("after_if_default")
      jump_after_if = DirectJump.new(after_if)
      node.stmt_list.each do |cond, stmts|
        cond_tac = visit(cond)
        true_label = generate_label
        cond_jump = CondJump.new(cond_tac, true_label, nil)
        @tac_list.push cond_jump
        @tac_list.push true_label
        visit(stmts)
        @tac_list.push jump_after_if
        false_label = generate_label
        cond_jump.false_addr = false_label
        @tac_list.push false_label
      end
      visit(node.else_stmts)
      @tac_list.push jump_after_if
      new_label = generate_label
      after_if.name = new_label.name
      @tac_list.push after_if
    end

    def on_function(fun)
      initialize
      @tac_list.push Label.new(fun.name)
      visit(fun.stmts)
      # return value store into a temp name
      @tac_list.push Return.new(@tac_list[-1])
      # todo:this jump need process, when return after this, maybe set a return in function is ok
      # used for BasicBlock
      @tac_list.push DirectJump.new(Label.new("TempReturnLabel"))
      Function.new(fun.name, @tac_list)
    end

    def on_lambda(node) end

    def on_fun_call(node)
      # todo:need process this and new expr
      args = node.args.map {|a| visit(a)}
      @tac_list.push Call.new(node.name, args)
    end

    def on_class_member_access(access) end

    def on_identifier(node)
      Name.new(node.name)
    end

    def on_instance(node) end

    def on_new_expr(node)
      mem = get_tmp_name
      alloc = Alloc.new(node.class_name, mem)
      args = node.args.map {|a| visit(a)}
      call = Call.new(node.class_name, [alloc] + args)
      @tac_list.push alloc
      @tac_list.push call
    end

    def on_constant(node) end

    def on_bool_constant(node)
      # this is a hack
      Number.new(node.val.to_i)
    end

    def on_number_constant(node)
      Number.new(node.val.to_i)
    end

    def on_string_constant(node)
      Memory.new(@const_table.add(node.val))
    end
  end
end
