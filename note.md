regexp
char '' or ""
\[0-9] \[a-z]
all char .

rule foo
  "the dog " bar
end
rule bar
  "jumped"
end
rule foo
  "the dog jumped"
end

"a"/"b"/"c"

"foo" "bar" ("baz" / "bop")
*(0-n)
+(1-n)
?(0,1)
'foo'2..
'foo'3..5
'foo'..4

"foo" &"bar" is ok, but "foo" & "bar" is err 
"foo" &"bar" only scan foo, if read "foobar", will not to reach end
"foo" &"bar" "bar" is ok, !is like &


如果?，不存在则elements为nil
单个字符匹配则是true，多个字符匹配则.elements.nil