require_relative 'translator'

module Rc
  module TAC
    class TACRoot
      # symbol store by str
      attr_reader :fun_list, :entry, :sym_table, :const_table

      def initialize(fun_list, sym_table, const_table)
        @entry = sym_table.fetch('main', nil)
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

      def first_fun_tac_list
        fun_list[0].tac_list
      end
    end

    def to_tac(ast, env)
      TacTranslator.new.translate(ast, env)
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

    class Memory
      attr_reader :addr

      def initialize(addr)
        @addr = addr
      end

      def ==(other)
        @addr == other.addr
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

      def ==(other)
        @op == other.op && @result == other.result && @lhs == other.lhs && @rhs == other.rhs
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

      def ==(other)
        @name == other.name
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

      def ==(other)
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

      def ==(other)
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

      def ==(other)
        @name == other.name
      end
    end

    class Empty
      def to_s
        ""
      end

      def ==(other)
        self.class == other.class
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

    module_function :to_tac
  end
end