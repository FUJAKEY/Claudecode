#ifndef GAME_CLIENT_COMPONENTS_FUJIX_AI_H
#define GAME_CLIENT_COMPONENTS_FUJIX_AI_H

#include <game/client/component.h>
#include <vector>
#include <base/vmath.h>

struct AStarNode
{
	int X, Y;
	int G, H;
	AStarNode *pParent;

	int F() const { return G + H; }
	bool operator==(const AStarNode &Other) const { return X == Other.X && Y == Other.Y; }
};

class CFujixAI : public CComponent
{
public:
	int Sizeof() const override { return sizeof(*this); }
	void OnRender() override;
	void OnInit() override;

private:
	void UpdateControls();
	
	// Pathfinding
	std::vector<vec2> m_Path;
	int m_PathIndex;
	int m_LastPathFindTime;
	
	void FindPath(vec2 Start, vec2 End);
	bool IsSolid(int x, int y) const;
};

#endif
