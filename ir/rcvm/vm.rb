require './ir/tac/visitor'
require './lib/env'
require_relative './inst'

module Rc::VM
  module MemoryLocation
    ESP = 0
    EAX = 1
    RBP = 2
  end

  def to_vm_inst(tac)
    RCVMInstTranslator.new.visit(tac)
    # sym_table = analysis_sym_table(list)
    # update_call_addr(list, sym_table)
  end

  def analysis_sym_table(list)
    sym_table = Env.new
    # todo: maybe a slow impl
    list.each_with_index do |inst, index|
      if inst.is_a? Label
        sym_table.define_symbol inst.name, index
      end
    end
    sym_table
  end

  def update_call_addr(list, sym_table)
    list.filter { |x| x.is_a? UnsetAddr }.map { |a| Addr.new('code', sym_table[a]) }
  end

  module VMEnv
    def get_symbol(inst)
      n = inst.name
      if @sym_table.has_symbol? n
        @sym_table[n]
      else
        @sym_table.define_symbol(n, @sym_table.size)
      end
    end
  end

  module QuadTranslator
    def on_quad(inst)
      if inst.rhs.is_a? Rc::TAC::EmptyValue
        raise "quad #{inst} rhs should not be empty value"
      end
      v1 = visit(inst.lhs)
      v2 = visit(inst.rhs)
      op = process_op(inst.op)
      res = visit(inst.result) # should be TempName, because assign
      [v1, v2, op, res]
    end

    def on_cond_jump(inst)
      # todo:fix this
      CondJump.new(inst.cond, UnsetAddr.new(inst.true_addr.name))
    end

    def on_assign(inst)
      if inst.target.is_a? Rc::TAC::TempName
        raise 'assign target should be name'
      end
      [visit(inst.value), SetLocal.new(get_symbol(inst.target))]
    end

    def on_call(inst)
      # todo: this and TempName has some error
      [inst.args.map { |arg| visit(arg) }, Call.new(inst.target)]
    end
  end

  module OperandTranslator
    include VMEnv

    def on_name(inst)
      # refactor
      GetLocal.new(get_symbol(inst))
    end

    def on_temp_name(inst)
      # nop
    end

    def on_memory(inst)
      Push.new(inst.addr)
    end

    def on_number(inst)
      Push.new(inst.num)
    end
  end

  class RCVMInstTranslator
    include Rc::TAC::Visitor
    include QuadTranslator
    include OperandTranslator

    attr_reader :sym_table

    def initialize
      @sym_table = Rc::Env.new
    end

    def on_tac_root(root)
      super(root).flatten.compact
    end

    def on_function(fun)
      fun.tac_list.map { |t| visit(t) }
    end

    def on_label(inst)
      Label.new(inst.name)
    end

    def on_direct_jump(inst)
      DirectJump.new(UnsetAddr.new(inst.target.name))
    end

    # todo:process this
    def on_move(inst)
      inst
    end

    def on_return(inst)
      # pop value to eax
      Pop.new(MemoryLocation::EAX)
      Return.new
    end

    def process_op(op)
      case op
      in "+"
        Add.new
      in "-"
        Sub.new
      in "*"
        Mul.new
      in "/"
        Div.new
      else
        raise "not supported op #{op}"
      end
    end

    def on_empty_op(inst)
      []
    end

    def on_empty_value(inst)
      []
    end
  end

  module_function :to_vm_inst, :analysis_sym_table, :update_call_addr
end
