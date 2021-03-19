module Rc
  class RcError < StandardError
    def initialize
    end

    def inspect
      "#{self.class} inspect not be implement"
    end
  end

  # run time error
  class RuntimeError < RcError
  end

  class ParserError < RcError
    # TODO:more detailed error info
    def initialize(line, column, reason)
      @line, @column, @reason = line, column, reason
    end

    def inspect
      "Error line:#{@line}, column:#{@column}\nreason:#{@reason}"
    end
  end

  class SemanticError < RcError
    def initialize
    end
  end

  class ArgsLengthNotMatchError < SemanticError
    def initialize(fun_name, param_count, argc)
      @fun_name = fun_name
      @param_count, @argc = param_count, argc
    end

    def inspect
      "in fun:#{@fun_name}\nrequired #{@param_count} but it's actually #{@argc}"
    end
  end

  class SymbolNotFoundError < SemanticError
    def initialize(sym)
      @sym = sym
    end

    def inspect
      "symbol #{@sym} not found"
    end
  end

  class SymbolReDefineError < SemanticError
    def initialize(sym)
      @sym = sym
    end

    def inspect
      "symbol #{@sym} redefine"
    end
  end

  class ClassMemberNotFound < SemanticError
    def initialize(instance_name, instance, member)
      @instance_name = instance_name
      @instance, @member = instance, member
    end

    def inspect
      "class#{@instance.class_define.name} instance:#{@instance_name} member:#{@member}"
    end
  end

  class UnFinishedError < RcError
    def initialize(node)
      @node = node
    end

    def inspect
      "node:#{@node} unfinished"
    end
  end

  class ExprError < Rc::RuntimeError

  end

  class UnknownError < RcError
    def initialize(info)
      @info = info
    end

    def inspect
      "Unknown error, info:#{@info}"
    end
  end

  class AssertFailedError < Rc::RuntimeError
    def initialize(a, b)
      @a, @b = a, b
    end

    def inspect
      "#{@a} != #{@b}"
    end
  end
end