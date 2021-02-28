import 'parser'
import "set"

def main
  var set = Set.new(1, 2, num)
  var foo = Foo.new()
  # set.v
  set.uniq()

  var b1 = true
  var b2 = false

  var num = 1
  num = 2
  var s1 = 'str'
  var s2 = "str"

  f1(1, 2, num)

  var id = num

  if true
    var m = 1
    var s = 2
  elsif false
    var q = 2
    var a = 1
  elsif false
    var n = 10
    var q = 10
  else
    var t = 22
    var n = 2
  end
  return 1
  return num
  break
end

def f1(a, b, c)
  1 + 1 + 1
end

def f2()

end

def f3(a, b, c, argc, argv)

end


class Foo

end

class Set < Foo
  def init(a, b, c)

  end

  var a

  def uniq
    var b = 1
  end
end