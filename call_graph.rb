# single f can call multi f
# single f can be called by multi f

module Rc
  class CallGraph
    # visit a function, add all call
    @call_map = {}
    # caller -> multi callee
    def analysis_fun(fun)
      @call_map[fun.name] = visit_stmts
    end

    # reverse key and map
    def find_usage(fun)

    end
  end
end