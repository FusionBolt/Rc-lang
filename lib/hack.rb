class Object
  def try(*a, &b)
    if a.empty? && block_given?
      yield self
    else
      public_send(*a, &b) if respond_to?(a.first)
    end
  end

  def or_else(&block)
    self
  end
end

class NilClass
  def try(*args)
    nil
  end

  def or_else(&block)
    block.call
  end
end

class FalseClass; def to_i; 0 end end
class TrueClass; def to_i; 1 end end