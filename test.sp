import 'parser'
import "set"

var a = 1 + 2 + 3 + 4 +5
var b = a

def f(int m, n, int q, m)
  var a = 1
end

def main
  var m = 1 + 2 + 3 + 4
  var n = m
  var s = "str"
  var q = true
  f(1, 2, 3)
  f(m, n)
  f(1+1, 2*2)
#  f(1, 2)
#  f(m, n)
end

class Set

  var a

  def f1
    var b = 1

    if 5
      var m = q
      var m = q
    elsif 6
      var q = 2
      var a = 1
    else
      var t = 22
      var n = 2
    end

    return
  end

end


  rule identifier
    !keyword name:([a-zA-Z] [a-zA-Z0-9_]*) {
      def inspect
        input[interval]
      end

      def eval(env = {})
        env[inspect]
      end
    }
  end

       (&(identifier '(') fun_call)