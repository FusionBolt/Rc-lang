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

  def demodulize_class
    self.class.to_s.demodulize
  end
end

class Class
  def demodulize
    self.to_s.demodulize
  end
end

class String
  def demodulize
    self.split('::').last
  end

  def underscore
    self.gsub(/::/, '/').
      gsub(/([A-Z]+)([A-Z][a-z])/, '\1_\2').
      gsub(/([a-z\d])([A-Z])/, '\1_\2').
      tr("-", "_").
      downcase
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