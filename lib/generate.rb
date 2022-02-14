class Array
  def generate(c = "\n", &f)
    map {|a| f[a] }.join(c)
  end

  def pure_generate(&f)
    map { |a| a.demodulize_class }.generate(&f)
  end
end
