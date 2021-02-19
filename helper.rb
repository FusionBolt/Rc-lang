def import_source_file(name)
  package = name[3].input[name[3].interval]
  p "import package:#{package}"
end

def debug(data)
  data[0]
end