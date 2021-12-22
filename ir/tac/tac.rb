require_relative '../ast/visitor'

module Rc
  module TAC
    class TACRoot
      # symbol store by str
      attr_accessor :fun_list, :entry, :sym_table, :const_table

      def initialize(fun_list, env, sym_table, const_table)
        @entry = env.fetch('main', nil)
        @fun_list = fun_list
        @sym_table, @const_table = sym_table, const_table
      end

      def to_s
        <<TOS
entry:#{@entry}
#{@fun_list.map(&:to_s).join("\n")}
TOS
      end

      def process(&proc_f)
        @fun_list.map! { |fun| proc_f.call(fun) }
      end
    end

    def to_tac(ast, env)
      fun_list = TacTranslator.new.visit(ast)
      TACRoot.new(fun_list, env, {}, {})
    end

    class Function
      attr_reader :name, :tac_list

      def initialize(name, tac_list)
        @name, @tac_list = name, tac_list
      end

      def [](index)
        @tac_list[index]
      end
    end

    class Quad
      # op: binary, alloc
      attr_accessor :op, :result, :lhs, :rhs

      def initialize(op, result, lhs, rhs)
        @op, @result, @lhs, @rhs = op, result, lhs, rhs
      end

      def to_s
        "#{@result} = #{@lhs} #{@op} #{@rhs}"
      end
    end

    class Name
      attr_accessor :name

      def initialize(name)
        @name = name
      end

      def to_s
        @name.gsub(/:/, '')
      end
    end

    # likely reg
    class TempName < Name
    end

    class Number
      attr_accessor :num

      def initialize(num)
        @num = num
      end

      def to_s
        @num.to_s
      end

      def eql?(other)
        @num == other.num
      end
    end

    class Loop
    end

    class Jump
    end

    class CondJump < Jump
      attr_accessor :cond, :true_addr, :false_addr

      def initialize(cond, true_addr, false_addr)
        @cond, @true_addr, @false_addr = cond, true_addr, false_addr
      end

      def to_s
        "Cond Jump: #{@cond}? #{true_addr} #{false_addr}"
      end

      def deconstruct
        [@cond, @true_addr, @false_addr]
      end
    end

    class DirectJump < Jump
      attr_accessor :target

      def initialize(target)
        @target = target
      end

      def to_s
        "Direct Jump to #{target}"
      end

      def deconstruct
        [@target]
      end

      def eql?(other)
        @target == other.target
      end
    end

    class Label
      attr_accessor :name

      def initialize(name)
        @name = name
      end

      def to_s
        @name
      end

      def eql?(other)
        @name == other.name
      end
    end

    class Empty
      def to_s
        ""
      end
    end

    class EmptyOp < Empty
    end

    class EmptyValue < Empty
    end

    class Move
      attr_accessor :expr, :target

      def initialize(expr, target)
        @expr, @target = expr, target
      end
    end

    class Return
      attr_accessor :ret_val

      def initialize(ret_val)
        @ret_val = ret_val
      end
    end

    class Alloc
      attr_accessor :type, :result

      def initialize(type, result)
        @type, @result = type, result
      end
    end

    class Call
      attr_accessor :target, :args

      def initialize(target, args)
        @target, @args = target, args
      end
    end

    # todo:mixin code which not visitor
    class TacTranslator
      include AST::Visitor
      attr_reader :tac_list

      def initialize
        local_init
        @const_table = {}
        @sym_table = {}
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

      def on_string_constant(node) end
    end
    module_function :to_tac
  end
end