require './lib/type_struct'

module TypeStructTest
  class Label < TypeStruct.new(:name)
  end

  class SetLocal < TypeStruct.new(:offset => :int)
  end
end

describe 'TypeStruct' do
  it "default type" do
    l = TypeStructTest::Label.new('str')
    expect(l.name).to eq 'str'
    expect(l.name_t).to eq :str
  end

  it "spec type" do
    s = TypeStructTest::SetLocal.new(5)
    expect(s.offset).to eq 5
    expect(s.offset_t).to eq :int
  end

  it "type check" do
    expect {
      class ErrType < TypeStruct.new(:var => :what?)
      end
    }.to raise_error RuntimeError
  end
end

